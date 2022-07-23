package joey.mqtt.broker.constant;

/**
 * 业务常量类
 *
 * @author Joey
 * @date 2019/7/18
 */
public class BusinessConstants {
    public static final String MQTT_CONFIG = "mqtt.conf";

    public static final String MQTT_CONFIG_PROPS_PRE = "mqtt";

    public static final String MQTT_SUB_PROTOCOL_CSV_LIST = "mqtt, mqttv3.1, mqttv3.1.1";

    /**
     * netty handler名称常量
     */
    public static final String HANDLER_IDLE_STATE = "idleStateHandler";

    public static final String HANDLER_SSL = "sslHandler";

    public static final String HANDLER_HTTP_CODEC = "httpCodecHandler";

    public static final String HANDLER_HTTP_AGGREGATOR = "httpAggregatorHandler";

    public static final String HANDLER_HTTP_COMPRESSOR = "httpCompressorHandler";

    public static final String HANDLER_WEB_SOCKET_SERVER_PROTOCOL = "webSocketServerProtocolHandler";

    public static final String HANDLER_MQTT_WEB_SOCKET_CODEC = "mqttWebSocketCodecHandler";

    public static final String HANDLER_MQTT_ENCODER = "mqttEncoderHandler";

    public static final String HANDLER_MQTT_DECODER = "mqttDecoderHandler";

    public static final String HANDLER_MQTT_MAIN = "mqttMainHandler";

    /**
     * topic token常量
     */
    public static final String TOKEN_ROOT = "joRootTopic";

    public static final String TOKEN_MULTI = "#";

    public static final String TOKEN_SINGLE = "+";

    /**
     * hazelcast先关基本设置
     */
    public static final String HAZELCAST_KEY_PRE = "joHz:";

    /**
     * 使用hazelcast作为集群通信时的topic
     */
    public static final String HAZELCAST_INNER_TRAFFIC_TOPIC = HAZELCAST_KEY_PRE + "innerTraffic";

    public static final String HAZELCAST_MSG_ID = HAZELCAST_KEY_PRE + "msgId";

    public static final String HAZELCAST_SUB_STORE = HAZELCAST_KEY_PRE + "subStore";

    public static final String HAZELCAST_MSG_RETAIN = HAZELCAST_KEY_PRE + "msgRetain";

    public static final String HAZELCAST_MSG_DUP_PUB = HAZELCAST_KEY_PRE + "msgDupPub";

    public static final String HAZELCAST_MSG_DUP_PUB_REL = HAZELCAST_KEY_PRE + "msgDupPubRel";

    /**
     * redis相关基本设置
     */
    public static final Integer REDIS_EACH_SCAN_COUNT = NumConstants.INT_500;

    public static final String REDIS_KEY_PRE = "joMqtt:";

    public static final String REDIS_MSG_ID_KEY_PRE = REDIS_KEY_PRE + "msg:";

    public static final String REDIS_MSG_ID_FIELD = "id";

    public static final String REDIS_SUB_STORE_KEY = REDIS_KEY_PRE + "subStore:";

    public static final String REDIS_MSG_RETAIN_KEY = REDIS_KEY_PRE + "msgRetain";

    public static final String REDIS_MSG_DUP_PUB_KEY_PRE = REDIS_KEY_PRE + "msgDupPub:";

    public static final String REDIS_MSG_DUP_PUB_REL_KEY_PRE = REDIS_KEY_PRE + "msgDupPubRel:";

    public static final String REDIS_INNER_TRAFFIC_PUB_CHANNEL = REDIS_KEY_PRE + "innerTrafficChannel";
}
