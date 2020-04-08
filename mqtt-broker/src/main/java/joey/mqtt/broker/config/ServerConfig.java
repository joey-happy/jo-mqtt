package joey.mqtt.broker.config;

import joey.mqtt.broker.auth.AuthUser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务配置
 * @author Joey
 * @date 2019/7/18
 */
@Getter
@Setter
@ToString
public class ServerConfig {
    /**
     * tcp端口 -1表示不启动
     */
    private int tcpPort = 1883;

    /**
     * tcp-ssl端口 -1表示不启动
     */
    private int tcpSslPort = 1888;

    /**
     * webSocket访问路径
     */
    private String webSocketPath = "/joMqtt";

    /**
     * webSocket端口 -1表示不启动
     */
    private int webSocketPort = 2883;

    /**
     * websocket-ssl端口 -1表示不启动
     */
    private int webSocketSslPort = 2888;

    /**
     * 开启用户CA认证
     */
    private boolean enableClientCA = false;

    private String hostname;

    /**
     * extendProvider接口实现的类全路径名称
     */
    private String extendProviderClass = "joey.mqtt.broker.provider.adapter.ExtendProviderAdapter";

    /**
     * 开启用户名密码认证
     */
    private boolean enableUserAuth = true;

    /**
     * 授权用户名和密码list
     */
    private List<AuthUser> authUsers = new ArrayList<>();
}
