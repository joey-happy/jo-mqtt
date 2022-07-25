package joey.mqtt.broker.event.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.PubCompEventMessage;
import joey.mqtt.broker.store.IDupPubRelMessageStore;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * pubComp事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class PubCompEventProcessor implements IEventProcessor<MqttMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final IDupPubRelMessageStore pubRelMessageStore;

    private final EventListenerExecutor eventListenerExecutor;

    public PubCompEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, IDupPubRelMessageStore pubRelMessageStore, EventListenerExecutor eventListenerExecutor) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.pubRelMessageStore = pubRelMessageStore;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage message) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader)message.variableHeader();
        int messageId = variableHeader.messageId();

        String clientId = NettyUtils.clientId(ctx.channel());
        String userName = NettyUtils.userName(ctx.channel());

        pubRelMessageStore.remove(clientId, messageId);

        eventListenerExecutor.execute(new PubCompEventMessage(clientId, userName, messageId), IEventListener.Type.PUB_COMP);
    }
}
