package joey.mqtt.broker.event.processor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.core.dispatcher.DispatcherResult;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.PublishEventMessage;
import joey.mqtt.broker.exception.MqttException;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.store.*;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import joey.mqtt.broker.util.Stopwatch;
import joey.mqtt.broker.util.TopicUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 发布事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class PublishEventProcessor implements IEventProcessor<MqttPublishMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final ISessionStore sessionStore;

    private final ISubscriptionStore subStore;

    private final IMessageIdStore messageIdStore;

    private final IRetainMessageStore retainMessageStore;

    private final IDupPubMessageStore dupPubMessageStore;

    private final EventListenerExecutor eventListenerExecutor;

    private final String nodeName;

    @Setter
    private IInnerTraffic innerTraffic;

    public PublishEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, ISessionStore sessionStore, ISubscriptionStore subStore, IMessageIdStore messageIdStore, IRetainMessageStore retainMessageStore, IDupPubMessageStore dupPubMessageStore, EventListenerExecutor eventListenerExecutor, String nodeName) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
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

        CommonPublishMessage pubMsg = CommonPublishMessage.convert(clientId, message, false, nodeName);

        String publishTopic = pubMsg.getTopic();
        if (CollUtil.isEmpty(TopicUtils.getTopicTokenList(publishTopic))) {
            throw new MqttException("Publish invalid topic. topic=" + publishTopic);
        }

        Stopwatch stopwatch = Stopwatch.start();
        log.info("Process-publish start. pubMessage={}", pubMsg);

        //集群间发送消息
        try {
            innerTraffic.publish(pubMsg);
            log.info("Process-publish publish message to cluster end. clientId={},userName={},topic={},timeCost={}ms", clientId, userName, pubMsg.getTopic(), stopwatch.elapsedMills());
        } catch (Exception ex) {
            log.error("PublishEventProcessor-process inner traffic publish error.", ex);
        }

        int packetId = message.variableHeader().packetId();
        MqttQoS msgQoS = MqttQoS.valueOf(pubMsg.getMqttQoS());
        boolean validQos = true;

        switch (msgQoS) {
            case AT_MOST_ONCE:
                dispatcherCommandCenter.dispatch(clientId, "Pub qos0", () -> {
                    publish2Subscribers(pubMsg);

                    //处理保留消息
                    handleRetainMessage(pubMsg);
                    return null;
                });
                break;

            case AT_LEAST_ONCE:
                dispatcherCommandCenter.dispatch(clientId, "Pub qos1", () -> {
                    publish2Subscribers(pubMsg);

                    //处理保留消息
                    handleRetainMessage(pubMsg);
                    return null;
                });

                MqttPubAckMessage pubAckResp = MessageUtils.buildPubAckMessage(packetId);
                ctx.channel().writeAndFlush(pubAckResp);
                break;

            case EXACTLY_ONCE:
                dispatcherCommandCenter.dispatch(clientId, "Pub qos2", () -> {
                    publish2Subscribers(pubMsg);

                    //处理保留消息
                    handleRetainMessage(pubMsg);
                    return null;
                });

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
     * 批量处理 将订阅者按照clientId分组之后 交给多线程处理 提升分发效率
     *
     * @param pubMsg
     */
    public void publish2Subscribers(CommonPublishMessage pubMsg) {
        List<Subscription> matchSubList = subStore.match(pubMsg.getTopic());

        if (CollUtil.isNotEmpty(matchSubList)) {
            BatchSubCollector collector = new BatchSubCollector(dispatcherCommandCenter.getDispatcherCount());
            for (Subscription sub : matchSubList) {
                collector.add(sub);
            }

            collector.execute(subscriptionList -> {
                for (Subscription sub : subscriptionList) {
                    publish2Subscriber(sub, pubMsg);
                }
            });
        }
    }

    /**
     * 发布消息到指定订阅者
     *
     * @param sub
     * @param pubMsg
     * @return
     */
    boolean publish2Subscriber(Subscription sub, CommonPublishMessage pubMsg) {
        Stopwatch start = Stopwatch.start();

        boolean sendResult = doPublish2Subscriber(sub, pubMsg);
        log.info("Process-publish to sub finished. targetClientId={},topic={},sendResult={},timeCost={}ms", sub.getClientId(), pubMsg.getTopic(), sendResult, start.elapsedMills());

        return sendResult;
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

        if (ObjectUtil.isNotNull(targetSession)) {
            //订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
            //参考说明：https://www.emqx.com/zh/blog/mqtt-qos-design-for-internet-of-vehicles
            MqttQoS msgQoS = MqttQoS.valueOf(MessageUtils.getMinQos(commonPubMsg.getMqttQoS(), sub.getQos().value()));

            MqttPublishMessage mqttPubMsg = null;
            int messageId = NumConstants.INT_0;

            try {
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
            } catch (Throwable t) {
                log.error("Process-publish error. targetClientId={},topic={},qos={}", targetClientId, commonPubMsg.getTopic(), commonPubMsg.getMqttQoS(), t);
                return false;
            }
        }

        return true;
    }

    /**
     * 处理retainMessage
     *
     * @param pubMsg
     */
    void handleRetainMessage(CommonPublishMessage pubMsg) {
        if (pubMsg.isRetain()) {
            //遗言消息
            if (pubMsg.isWill()) {
                //覆盖retain消息
                retainMessageStore.add(pubMsg.copy());
                return;
            }

            //正常pub消息 如果消息为空 则清除retain消息
            if (StrUtil.isEmpty(pubMsg.getMessageBody())) {
                retainMessageStore.remove(pubMsg.getTopic());
                return;
            }

            //覆盖retain消息
            retainMessageStore.add(pubMsg.copy());
        }
    }

    /**
     * 批量订阅者收集器
     * 参考: moquette BatchingPublishesCollector
     *
     * todo 日志打印
     *
     */
    private class BatchSubCollector {
        private final List<Subscription>[] subscriptionListArray;
        private final int dispatcherCount;

        BatchSubCollector(int dispatcherCount) {
            this.subscriptionListArray = new ArrayList[dispatcherCount];
            this.dispatcherCount = dispatcherCount;
        }

        void add(Subscription sub) {
            int dispatcherIndex = Math.abs(sub.getClientId().hashCode()) % this.dispatcherCount;
            if (subscriptionListArray[dispatcherIndex] == null) {
                subscriptionListArray[dispatcherIndex] = new ArrayList<>();
            }

            subscriptionListArray[dispatcherIndex].add(sub);
        }

        List<DispatcherResult> execute(Consumer<List<Subscription>> action) {
            List<DispatcherResult> publishResults = new ArrayList<>(this.dispatcherCount);

            for (List<Subscription> subscriptionList : subscriptionListArray) {
                if (CollUtil.isEmpty(subscriptionList)) {
                    continue;
                }

                publishResults.add(dispatcherCommandCenter.dispatch(CollUtil.getFirst(subscriptionList).getClientId(), "Pub batch", () -> {
                    action.accept(subscriptionList);
                    return null;
                }));
            }

            return publishResults;
        }
    }
}
