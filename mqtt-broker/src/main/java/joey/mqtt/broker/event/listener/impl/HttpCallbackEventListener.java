package joey.mqtt.broker.event.listener.impl;

import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.event.listener.adapter.EventListenerAdapter;
import joey.mqtt.broker.event.message.*;
import lombok.extern.slf4j.Slf4j;

/**
 * http回调事件监听器
 *
 * @author Joey
 * @date 2021/05/27
 */
@Slf4j
public class HttpCallbackEventListener extends EventListenerAdapter {
    public HttpCallbackEventListener(CustomConfig customConfig) {
        super(customConfig);
    }

    @Override
    public void onConnect(ConnectEventMessage connectMessage) {
        log.debug("Event-connect trigger. clientId={},userName={}", connectMessage.getClientId(), connectMessage.getUserName());
    }

    @Override
    public void onDisconnect(DisconnectEventMessage disconnectMessage) {
        log.debug("Event-disconnect trigger. clientId={},userName={}", disconnectMessage.getClientId(), disconnectMessage.getUserName());
    }

    @Override
    public void onConnectionLost(ConnectionLostEventMessage connectionLostMessage) {
        log.debug("Event-connectionLost trigger. clientId={},userName={}", connectionLostMessage.getClientId(), connectionLostMessage.getUserName());
    }

    @Override
    public void onPublish(PublishEventMessage publishMessage) {
        log.debug("Event-publish trigger. clientId={},userName={},topic={}", publishMessage.getClientId(), publishMessage.getUserName(), publishMessage.getTopic());
    }

    @Override
    public void onPubAck(PubAckEventMessage pubAckEventMessage) {
        log.debug("Event-pubAck trigger. clientId={},userName={},messageId={}", pubAckEventMessage.getClientId(), pubAckEventMessage.getUserName(), pubAckEventMessage.getMessageId());
    }

    @Override
    public void onPubRec(PubRecEventMessage pubRecEventMessage) {
        log.debug("Event-pubRec trigger. clientId={},userName={},messageId={}", pubRecEventMessage.getClientId(), pubRecEventMessage.getUserName(), pubRecEventMessage.getMessageId());
    }

    @Override
    public void onPubRel(PubRelEventMessage pubRelEventMessage) {
        log.debug("Event-pubRel trigger. clientId={},userName={},messageId={}", pubRelEventMessage.getClientId(), pubRelEventMessage.getUserName(), pubRelEventMessage.getMessageId());
    }

    @Override
    public void onPubComp(PubCompEventMessage pubCompEventMessage) {
        log.debug("Event-pubComp trigger. clientId={},userName={},messageId={}", pubCompEventMessage.getClientId(), pubCompEventMessage.getUserName(), pubCompEventMessage.getMessageId());
    }

    @Override
    public void onSubscribe(SubscribeEventMessage subscribeMessage) {
        log.debug("Event-subscribe trigger. clientId={},userName={},topic={}", subscribeMessage.getClientId(), subscribeMessage.getUserName(), subscribeMessage.getTopic());
    }

    @Override
    public void onUnsubscribe(UnsubscribeEventMessage unsubscribeMessage) {
        log.debug("Event-unsubscribe trigger. clientId={},userName={},topic={}", unsubscribeMessage.getClientId(), unsubscribeMessage.getUserName(), unsubscribeMessage.getTopic());
    }

    @Override
    public void onPing(PingEventMessage pingEventMessage) {
        log.debug("Event-ping trigger. clientId={},userName={}", pingEventMessage.getClientId(), pingEventMessage.getUserName());
    }
}
