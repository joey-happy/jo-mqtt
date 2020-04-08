package joey.mqtt.broker.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * sslContext配置
 * @author Joey
 * @date 2020/4/3
 */
@Getter
@Setter
@ToString
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
