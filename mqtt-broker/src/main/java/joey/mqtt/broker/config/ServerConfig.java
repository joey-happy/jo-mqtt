package joey.mqtt.broker.config;

import cn.hutool.core.util.StrUtil;
import joey.mqtt.broker.auth.AuthUser;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.provider.ExtendProviderAdapter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务配置
 * @author Joey
 * @date 2019/7/18
 */
@Data
public class ServerConfig {
    /**
     * tcp端口 -1表示不启动
     */
    private int tcpPort = BusinessConstants.MQTT_TCP_PORT;

    /**
     * tcp-ssl端口 -1表示不启动 1888
     */
    private int tcpSslPort = NumConstants.INT_NEGATIVE_1;

    /**
     * webSocket访问路径
     */
    private String webSocketPath = "/joMqtt";

    /**
     * webSocket端口 -1表示不启动 2883
     */
    private int webSocketPort = NumConstants.INT_NEGATIVE_1;

    /**
     * websocket-ssl端口 -1表示不启动 2888
     */
    private int webSocketSslPort = NumConstants.INT_NEGATIVE_1;

    /**
     * 开启用户CA认证
     */
    private boolean enableClientCA = false;

    private String hostname = StrUtil.EMPTY;

    /**
     * extendProvider接口实现的类全路径名称
     */
    private String extendProviderClass = ExtendProviderAdapter.class.getName();

    /**
     * 是否开启用户名密码认证
     */
    private boolean enableUserAuth = false;

    /**
     * 授权用户名和密码list
     */
    private List<AuthUser> authUsers = new ArrayList<>();

    /**
     * 分发器数量
     * todo 此处设置动态调整
     */
    private Integer dispatcherCount = Runtime.getRuntime().availableProcessors();

    /**
     * 每个分发器处理队列大小
     */
    private Integer dispatcherQueueSize = NumConstants.INT_1024;
}
