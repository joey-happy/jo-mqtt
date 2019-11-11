package joey.mqtt.broker.event.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.PubRelEventMessage;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * pubRel事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class PubRelEventProcessor implements IEventProcessor<MqttMessage> {
    private final EventListenerExecutor eventListenerExecutor;

    public PubRelEventProcessor(EventListenerExecutor eventListenerExecutor) {
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage message) {
        String clientId = NettyUtils.clientId(ctx.channel());
        String userName = NettyUtils.userName(ctx.channel());

        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) message.variableHeader();
        int messageId = variableHeader.messageId();
        ctx.channel().writeAndFlush(MessageUtils.buildPubCompMessage(messageId));

        eventListenerExecutor.execute(new PubRelEventMessage(clientId, userName, messageId), IEventListener.Type.PUB_REL);
    }
}
