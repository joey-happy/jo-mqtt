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
     * Key文件路径
     * 文件绝对路径或者classPath路径
     */
    private String sslKeyFilePath;

    /**
     * keyStore类型
     */
    private String sslKeyStoreType;

    /**
     * manager密码
     */
    private String sslManagerPwd;

    /**
     * store密码
     */
    private String sslStorePwd;
}
