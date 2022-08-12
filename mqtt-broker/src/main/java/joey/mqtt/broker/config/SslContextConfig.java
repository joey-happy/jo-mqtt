package joey.mqtt.broker.config;

import lombok.Data;

/**
 * sslContext配置
 * @author Joey
 * @date 2020/4/3
 */
@Data
public class SslContextConfig {
    /**
     * ssl支持
     * @see io.netty.handler.ssl.SslProvider
     */
    private String sslProvider;

    /**
     * jks文件绝对路径或者classPath路径
     */
    private String jksFilePath;

    /**
     * keyStore类型
     */
    private String keyStoreType;

    /**
     * 打开keyStore密码
     */
    private String keyStorePassword;

    /**
     * 管理keyStore中别名密码
     */
    private String keyManagerPassword;
}
