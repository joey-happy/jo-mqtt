package joey.mqtt.broker.event.processor;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.PubAckEventMessage;
import joey.mqtt.broker.store.IDupPubMessageStore;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * pubAck事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class PubAckEventProcessor implements IEventProcessor<MqttPubAckMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final IDupPubMessageStore dupPubMessageStore;

    private final EventListenerExecutor eventListenerExecutor;

    public PubAckEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, IDupPubMessageStore dupPubMessageStore, EventListenerExecutor eventListenerExecutor) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.dupPubMessageStore = dupPubMessageStore;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttPubAckMessage message) {
        int messageId = message.variableHeader().messageId();

        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);
        String userName = NettyUtils.userName(channel);

        if (StrUtil.isNotBlank(clientId)) {
            dupPubMessageStore.remove(clientId, messageId);
        }

        eventListenerExecutor.execute(new PubAckEventMessage(clientId, userName, messageId), IEventListener.Type.PUB_ACK);
    }
}
