package joey.mqtt.broker.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import joey.mqtt.broker.config.SslContextConfig;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.exception.MqttException;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * SslContext工具类
 * 参考: moquette DefaultMoquetteSslContextCreator
 *
 * @author Joey
 * @date 2020-04-03
 */
@Slf4j
public class SslContextUtils {
    private SslContextUtils() {

    }

    public static SslContext build(boolean enableClientCA, SslContextConfig cfg) throws Exception {
        String keyStorePassword = cfg.getKeyStorePassword();
        if (StrUtil.isBlank(keyStorePassword)) {
            throw new MqttException("Ssl key store password invalid.");
        }

        KeyStore keyStore = KeyStore.getInstance(cfg.getKeyStoreType());
        try (InputStream ksInputStream = ResourceUtil.getStream(cfg.getJksFilePath())) {
            keyStore.load(ksInputStream, keyStorePassword.toCharArray());
        }

        SslProvider sslProvider = getSSLProvider(cfg.getSslProvider());
        if (ObjectUtil.isNull(sslProvider)) {
            throw new MqttException("Ssl provider not support. provider=" + cfg.getSslProvider());
        }

        String keyManagerPassword = cfg.getKeyManagerPassword();
        if (StrUtil.isBlank(keyManagerPassword)) {
            throw new MqttException("Ssl key manager password invalid.");
        }

        SslContextBuilder contextBuilder = null;
        switch (sslProvider) {
            case JDK:
                contextBuilder = buildJdkProvider(keyStore, keyManagerPassword);
                break;

            case OPENSSL:
            case OPENSSL_REFCNT:
                contextBuilder = buildOpenSSLProvider(keyStore, keyManagerPassword);
                break;

            default:
                throw new MqttException("Ssl provider not support. provider=" + cfg.getSslProvider());
        }

        if (enableClientCA) {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            contextBuilder.trustManager(tmf);
            contextBuilder.clientAuth(ClientAuth.REQUIRE);
        }

        contextBuilder.sslProvider(sslProvider);
        SslContext sslContext = contextBuilder.build();
        log.info("Ssl context init successfully.");
        return sslContext;
    }

    private static SslProvider getSSLProvider(String providerName) {
        return SslProvider.valueOf(providerName);
    }

    private static SslContextBuilder buildJdkProvider(KeyStore ks, String keyManagerPassword) throws GeneralSecurityException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyManagerPassword.toCharArray());
        return SslContextBuilder.forServer(kmf);
    }

    private static SslContextBuilder buildOpenSSLProvider(KeyStore ks, String keyManagerPassword) throws GeneralSecurityException {
        for (String alias : Collections.list(ks.aliases())) {
            if (ks.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                PrivateKey key = (PrivateKey)ks.getKey(alias, keyManagerPassword.toCharArray());
                Certificate[] chain = ks.getCertificateChain(alias);
                X509Certificate[] certChain = new X509Certificate[chain.length];
                System.arraycopy(chain, NumConstants.INT_0, certChain, NumConstants.INT_0, chain.length);
                return SslContextBuilder.forServer(key, certChain);
            }
        }

        throw new MqttException("Ssl key store does not contain a private key");
    }
}
