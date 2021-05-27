package joey.mqtt.broker.util;

import cn.hutool.core.io.resource.ResourceUtil;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * SslContext工具类
 *
 * @author Joey
 * @date 2020-04-03
 */
@Slf4j
public class SslContextUtils {
    private SslContextUtils() {

    }

    public static SslContext build(boolean enableClientCA, String sslKeyFilePath, String sslKeyStoreType, String sslManagerPwd, String sslStorePwd) throws Exception {
        try (InputStream ksInputStream = ResourceUtil.getStream(sslKeyFilePath)) {
            KeyStore ks = KeyStore.getInstance(sslKeyStoreType);
            ks.load(ksInputStream, sslStorePwd.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, sslManagerPwd.toCharArray());
            SslContextBuilder contextBuilder = SslContextBuilder.forServer(kmf);

            if (enableClientCA) {
                contextBuilder.clientAuth(ClientAuth.REQUIRE);
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                contextBuilder.trustManager(tmf);
            }

            return contextBuilder.build();
        }
    }
}
