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
     * tcp端口
     */
    private int tcpPort = 1883;

    /**
     * webSocket端口 -1表示不启动webSocket
     */
    private int webSocketPort = -1;

    private String webSocketPath = "/mqtt";

    private String hostname;

    /**
     * extendProvider接口实现的类全路径名称
     */
    private String extendProviderClass = "joey.mqtt.broker.provider.adapter.ExtendProviderAdapter";

    /**
     * 开启用户名密码认证
     */
    private boolean enableAuth = true;

    /**
     * 授权用户名和密码list
     */
    private List<AuthUser> authUsers = new ArrayList<>();
}
