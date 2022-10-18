package joey.mqtt.broker.event.processor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import joey.mqtt.broker.auth.IAuth;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.enums.AuthTopicOperationEnum;
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
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final ISessionStore sessionStore;

    private final ISubscriptionStore subStore;

    private final IRetainMessageStore retainMessageStore;

    private final PublishEventProcessor publishEvent;

    private final EventListenerExecutor eventListenerExecutor;

    private final IAuth authManager;

    public SubscribeEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, ISessionStore sessionStore, ISubscriptionStore subStore, IRetainMessageStore retainMessageStore, PublishEventProcessor publishEvent, EventListenerExecutor eventListenerExecutor, IAuth authManager) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.sessionStore = sessionStore;
        this.subStore = subStore;
        this.retainMessageStore = retainMessageStore;
        this.publishEvent = publishEvent;
        this.eventListenerExecutor = eventListenerExecutor;
        this.authManager = authManager;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttSubscribeMessage message) {
        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);
        ClientSession clientSession = sessionStore.get(clientId);
        if (ObjectUtil.isNull(clientSession)) {
            channel.close();
            return;
        }

        dispatcherCommandCenter.dispatch(clientId, MqttMessageType.SUBSCRIBE, () -> {
            doSubscribe(clientId, channel, message);
            return null;
        });
    }

    /**
     * 订阅
     *
     * @param clientId
     * @param channel
     * @param message
     */
    private void doSubscribe(String clientId, Channel channel, MqttSubscribeMessage message) {
        List<MqttTopicSubscription> topicSubList = message.payload().topicSubscriptions();
        List<Integer> mqttQoSList = new ArrayList<>();

        //添加订阅关系
        topicSubList.forEach(topicSub -> {
            String topic = topicSub.topicName();

            if (!authManager.checkTopicAuth(clientId, topic, AuthTopicOperationEnum.READ)) {
                log.warn("Process-doSubscribe client id not auth to subscribe topic. clientId:{}, topic:{}", clientId, topic);
                mqttQoSList.add(MqttQoS.FAILURE.value());

            } else {
                MqttQoS mqttQoS = topicSub.qualityOfService();
                Subscription subscription = new Subscription(clientId, topic, mqttQoS);
                boolean addSuccess = subStore.add(subscription, false);
                if (addSuccess) {
                    eventListenerExecutor.execute(new SubscribeEventMessage(subscription, NettyUtils.userName(channel)), IEventListener.Type.SUBSCRIBE);
                }

                mqttQoSList.add(addSuccess ? mqttQoS.value() : MqttQoS.FAILURE.value());
            }
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
