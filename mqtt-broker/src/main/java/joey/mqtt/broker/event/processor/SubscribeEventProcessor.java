package joey.mqtt.broker.event.processor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.SubscribeEventMessage;
import joey.mqtt.broker.store.IRetainMessageStore;
import joey.mqtt.broker.store.ISessionStore;
import joey.mqtt.broker.store.ISubscriptionStore;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 订阅事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class SubscribeEventProcessor implements IEventProcessor<MqttSubscribeMessage> {
    private final ISessionStore sessionStore;

    private final ISubscriptionStore subStore;

    private final IRetainMessageStore retainMessageStore;

    private final PublishEventProcessor publishEvent;

    private final EventListenerExecutor eventListenerExecutor;

    public SubscribeEventProcessor(ISessionStore sessionStore, ISubscriptionStore subStore, IRetainMessageStore retainMessageStore, PublishEventProcessor publishEvent, EventListenerExecutor eventListenerExecutor) {
        this.sessionStore = sessionStore;
        this.subStore = subStore;
        this.retainMessageStore = retainMessageStore;
        this.publishEvent = publishEvent;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttSubscribeMessage message) {
        Channel channel = ctx.channel();
        List<MqttTopicSubscription> topicSubList = message.payload().topicSubscriptions();

        String clientId = NettyUtils.clientId(channel);
        ClientSession clientSession = sessionStore.get(clientId);

        if (ObjectUtil.isNull(clientSession)) {
            channel.close();
            return;
        }

        List<Integer> mqttQoSList = new ArrayList<>();

        //添加订阅关系
        topicSubList.forEach(topicSub -> {
            String topic = topicSub.topicName();
            MqttQoS mqttQoS = topicSub.qualityOfService();
            Subscription subscription = new Subscription(clientId, topic, mqttQoS);
            boolean addSuccess = subStore.add(subscription, false);
            if (addSuccess) {
                //处理监听事件
                eventListenerExecutor.execute(new SubscribeEventMessage(subscription, NettyUtils.userName(channel)), IEventListener.Type.SUBSCRIBE);
            }

            mqttQoSList.add(addSuccess ? mqttQoS.value() : MqttQoS.FAILURE.value());
        });

        MqttMessage ackResp = MessageUtils.buildSubAckMessage(message.variableHeader().messageId(), mqttQoSList);
        channel.writeAndFlush(ackResp);

        //发布retained消息
        topicSubList.forEach(topicSub -> {
            String topic = topicSub.topicName();
            MqttQoS mqttQoS = topicSub.qualityOfService();

            sendRetainMessage(new Subscription(clientId, topic, mqttQoS));
        });
    }

    /**
     * 发送保留消息
     *
     * @param sub
     */
    private void sendRetainMessage(Subscription sub) {
        List<CommonPublishMessage> retainMsgList = retainMessageStore.match(sub.getTopic());

        if (CollUtil.isNotEmpty(retainMsgList)) {
            for (CommonPublishMessage retainMessage : retainMsgList) {
                publishEvent.publish2Subscriber(sub, retainMessage);
            }
        }
    }
}
