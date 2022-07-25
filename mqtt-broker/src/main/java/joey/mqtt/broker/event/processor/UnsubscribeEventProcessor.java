package joey.mqtt.broker.event.processor;

import cn.hutool.core.collection.CollectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.UnsubscribeEventMessage;
import joey.mqtt.broker.store.ISessionStore;
import joey.mqtt.broker.store.ISubscriptionStore;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 取消订阅事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class UnsubscribeEventProcessor implements IEventProcessor<MqttUnsubscribeMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final ISessionStore sessionStore;

    private final ISubscriptionStore subStore;

    private final EventListenerExecutor eventListenerExecutor;

    public UnsubscribeEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, ISessionStore sessionStore, ISubscriptionStore subStore, EventListenerExecutor eventListenerExecutor) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.sessionStore = sessionStore;
        this.subStore = subStore;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttUnsubscribeMessage message) {
        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);

        dispatcherCommandCenter.dispatch(clientId, MqttMessageType.UNSUBSCRIBE, () -> {
            doUnsubscribe(clientId, channel, message);
            return null;
        });
    }

    /**
     * 取消订阅
     *
     * @param clientId
     * @param channel
     * @param message
     */
    private void doUnsubscribe(String clientId, Channel channel, MqttUnsubscribeMessage message) {
        String userName = NettyUtils.userName(channel);
        List<String> topicList = message.payload().topics();
        log.info("Process-unsubscribe. clientId={},userName={},topicList={}", clientId, userName, topicList);

        if (CollectionUtil.isNotEmpty(topicList)) {
            Optional.ofNullable(sessionStore.get(clientId))
                    .ifPresent(clientSession -> {
                        topicList.forEach(topic -> {
                            Subscription sub = new Subscription(clientId, topic, null);
                            subStore.remove(sub);
                            eventListenerExecutor.execute(new UnsubscribeEventMessage(topic, clientId, userName), IEventListener.Type.UNSUBSCRIBE);
                        });

                        MqttUnsubAckMessage unsubAckResp = MessageUtils.buildUnsubAckMessage(message.variableHeader().messageId());
                        channel.writeAndFlush(unsubAckResp);
                    });
        }
    }
}
