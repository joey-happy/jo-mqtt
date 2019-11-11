package joey.mqtt.broker.event.listener;

import joey.mqtt.broker.event.message.*;

/**
 * 事件监听器
 *
 * @author Joey
 * @date 2019/9/8
 */
public interface IEventListener {
    enum Type {
        CONNECT,
        DISCONNECT,
        CONNECTION_LOST,
        PUBLISH,
        PUB_ACK,
        PUB_REC,
        PUB_REL,
        PUB_COMP,
        SUBSCRIBE,
        UNSUBSCRIBE,
        PING;
    }

    /**
     * 连接事件
     * @param connectMessage
     */
    void onConnect(ConnectEventMessage connectMessage);

    /**
     * 断开连接事件
     * @param disconnectMessage
     */
    void onDisconnect(DisconnectEventMessage disconnectMessage);

    /**
     * 连接丢失事件
     * @param connectionLostMessage
     */
    void onConnectionLost(ConnectionLostEventMessage connectionLostMessage);

    /**
     * 发布事件
     * @param publishMessage
     */
    void onPublish(PublishEventMessage publishMessage);

    /**
     * 发布ack事件
     * @param pubAckEventMessage
     */
    void onPubAck(PubAckEventMessage pubAckEventMessage);

    /**
     * 发布rec事件
     * @param pubRecEventMessage
     */
    void onPubRec(PubRecEventMessage pubRecEventMessage);

    /**
     * 发布rel事件
     * @param pubRelEventMessage
     */
    void onPubRel(PubRelEventMessage pubRelEventMessage);

    /**
     * 发布comp事件
     * @param pubCompEventMessage
     */
    void onPubComp(PubCompEventMessage pubCompEventMessage);

    /**
     * 订阅事件
     * @param subscribeMessage
     */
    void onSubscribe(SubscribeEventMessage subscribeMessage);

    /**
     * 取消订阅事件
     * @param unsubscribeMessage
     */
    void onUnsubscribe(UnsubscribeEventMessage unsubscribeMessage);

    /**
     * ping事件
     * @param pingEventMessage
     */
    void onPing(PingEventMessage pingEventMessage);
}
