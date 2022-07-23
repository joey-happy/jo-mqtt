package joey.mqtt.broker.event.processor;

import cn.hutool.core.collection.CollectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
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
    private final ISessionStore sessionStore;

    private final ISubscriptionStore subStore;

    private final EventListenerExecutor eventListenerExecutor;

    public UnsubscribeEventProcessor(ISessionStore sessionStore, ISubscriptionStore subStore, EventListenerExecutor eventListenerExecutor) {
        this.sessionStore = sessionStore;
        this.subStore = subStore;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttUnsubscribeMessage message) {
        List<String> topicList = message.payload().topics();
        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);
        String userName = NettyUtils.userName(channel);

        log.info("Process-unsubscribe. clientId={},userName={},topicList={}", clientId, userName, topicList);
        if (CollectionUtil.isNotEmpty(topicList)) {
            Optional.ofNullable(sessionStore.get(clientId))
                    .ifPresent(clientSession -> {
                        topicList.forEach(topic -> {
                            Subscription sub = new Subscription(clientId, topic, null);
                            subStore.remove(sub);

                            //处理监听事件
                            eventListenerExecutor.execute(new UnsubscribeEventMessage(topic, clientId, userName), IEventListener.Type.UNSUBSCRIBE);
                        });

                        MqttUnsubAckMessage unsubAckResp = MessageUtils.buildUnsubAckMessage(message.variableHeader().messageId());
                        channel.writeAndFlush(unsubAckResp);
                    });
        }
    }
}
