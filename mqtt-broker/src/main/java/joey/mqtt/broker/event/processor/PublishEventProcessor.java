package joey.mqtt.broker.event.processor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.PublishEventMessage;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.store.*;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import joey.mqtt.broker.util.Stopwatch;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 发布事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class PublishEventProcessor implements IEventProcessor<MqttPublishMessage> {
    private final ISessionStore sessionStore;

    private final ISubscriptionStore subStore;

    private final IMessageIdStore messageIdStore;

    private final IRetainMessageStore retainMessageStore;

    private final IDupPubMessageStore dupPubMessageStore;

    private final EventListenerExecutor eventListenerExecutor;

    private final String nodeName;

    @Setter
    private IInnerTraffic innerTraffic;

    public PublishEventProcessor(ISessionStore sessionStore, ISubscriptionStore subStore, IMessageIdStore messageIdStore, IRetainMessageStore retainMessageStore, IDupPubMessageStore dupPubMessageStore, EventListenerExecutor eventListenerExecutor, String nodeName) {
        this.sessionStore = sessionStore;
        this.subStore = subStore;
        this.messageIdStore = messageIdStore;
        this.retainMessageStore = retainMessageStore;
        this.dupPubMessageStore = dupPubMessageStore;
        this.eventListenerExecutor = eventListenerExecutor;
        this.nodeName = nodeName;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttPublishMessage message) {
        String clientId = NettyUtils.clientId(ctx.channel());
        String userName = NettyUtils.userName(ctx.channel());

        CommonPublishMessage pubMsg = CommonPublishMessage.convert(message, false, nodeName);

        Stopwatch stopwatch = Stopwatch.start();
        log.info("Process-publish start. clientId={},userName={},topic={},messageId={},message={},qos={},nodeName={}", clientId, userName, pubMsg.getTopic(), pubMsg.getMessageId(), pubMsg.getMessageBody(), pubMsg.getMqttQoS(), nodeName);

        //集群间发送消息
        try {
            innerTraffic.publish(pubMsg);
        } catch (Exception ex) {
            log.error("PublishEventProcessor-process inner traffic publish error.", ex);
        }

        int packetId = message.variableHeader().packetId();
        MqttQoS msgQoS = MqttQoS.valueOf(pubMsg.getMqttQoS());
        boolean validQos = true;

        switch (msgQoS) {
            case AT_MOST_ONCE:
                publish2Subscribers(pubMsg);
                //处理保留消息
                handleRetainMessage(pubMsg);
                break;

            case AT_LEAST_ONCE:
                publish2Subscribers(pubMsg);
                //处理保留消息
                handleRetainMessage(pubMsg);

                MqttPubAckMessage pubAckResp = MessageUtils.buildPubAckMessage(packetId);
                ctx.channel().writeAndFlush(pubAckResp);
                break;

            case EXACTLY_ONCE:
                //TODO 当Qos=2时 应该在收到pubRel事件的时候,才应该触发触发消息发送,此处简化处理
                publish2Subscribers(pubMsg);
                //处理保留消息
                handleRetainMessage(pubMsg);

                MqttMessage pubRecResp = MessageUtils.buildPubRecMessage(packetId);
                ctx.channel().writeAndFlush(pubRecResp);
                break;

            default:
                validQos = false;
                break;
        }

        log.info("Process-publish end. pubClientId={},userName={},topic={},messageId={},message={},qos={},nodeName={},timeCost={}ms", clientId, userName, pubMsg.getTopic(), pubMsg.getMessageId(), pubMsg.getMessageBody(), pubMsg.getMqttQoS(), nodeName, stopwatch.elapsedMills());

        if (validQos) {
            //处理事件监听
            eventListenerExecutor.execute(new PublishEventMessage(message, clientId, userName), IEventListener.Type.PUBLISH);
        }
    }

    /**
     * 发布消息到所有订阅者
     *
     * @param pubMsg
     */
    public void publish2Subscribers(CommonPublishMessage pubMsg) {
        List<Subscription> matchSubList = subStore.match(pubMsg.getTopic());

        if (CollUtil.isNotEmpty(matchSubList)) {
            for (Subscription sub : matchSubList) {
                publish2Subscriber(sub, pubMsg);
            }
        }
    }

    /**
     * 发布消息到指定订阅者
     * @param sub
     * @param pubMsg
     * @return
     */
    void publish2Subscriber(Subscription sub, CommonPublishMessage pubMsg) {
        Stopwatch start = Stopwatch.start();

        try {
            if (doPublish2Subscriber(sub, pubMsg)) {
                log.info("Process-publish to sub successfully. targetClientId={},topic={},timeCost={}ms", sub.getClientId(), pubMsg.getTopic(), start.elapsedMills());
            }
        } catch (Throwable ex) {
            log.error("Process-publish to sub failure. targetClientId={},topic={},timeCost={}", sub.getClientId(), pubMsg.getTopic(), start.elapsedMills(), ex);
        }
    }

    /**
     * 发布消息到指定订阅者
     *
     * @param sub
     * @param commonPubMsg
     */
    boolean doPublish2Subscriber(Subscription sub, CommonPublishMessage commonPubMsg) {
        String targetClientId = sub.getClientId();
        ClientSession targetSession = sessionStore.get(targetClientId);

        if (null != targetSession) {
            //订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
            MqttQoS msgQoS = MqttQoS.valueOf(MessageUtils.getMinQos(commonPubMsg.getMqttQoS(), sub.getQos().value()));

            MqttPublishMessage mqttPubMsg = null;
            int messageId = 0;

            switch (msgQoS) {
                case AT_MOST_ONCE:
                    mqttPubMsg = MessageUtils.buildPubMsg(commonPubMsg, msgQoS, messageId);
                    targetSession.sendMsg(mqttPubMsg);
                    break;

                case AT_LEAST_ONCE:
                case EXACTLY_ONCE:
                    messageId = messageIdStore.getNextMessageId(targetClientId);
                    dupPubMessageStore.add(commonPubMsg.copy().setTargetClientId(targetClientId).setMessageId(messageId));

                    mqttPubMsg = MessageUtils.buildPubMsg(commonPubMsg, msgQoS, messageId);
                    targetSession.sendMsg(mqttPubMsg);
                    break;

                default:
                    log.error("Process-publish error. Invalid mqtt qos. targetClientId={},topic={},qos={}", targetClientId, commonPubMsg.getTopic(), commonPubMsg.getMqttQoS());
                    break;
            }

            return true;
        }

        return false;
    }

    /**
     * 处理retainMessage
     * @param pubMsg
     */
    void handleRetainMessage(CommonPublishMessage pubMsg) {
        if (pubMsg.isRetain()) {
            //如果消息为空 则清除retain消息
            if (StrUtil.isEmpty(pubMsg.getMessageBody())) {
                retainMessageStore.remove(pubMsg.getTopic());

            } else {
                //覆盖retain消息
                retainMessageStore.add(pubMsg.copy());
            }
        }
    }
}
