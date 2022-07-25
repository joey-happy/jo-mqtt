package joey.mqtt.broker.event.processor;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
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
        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);

        if (StrUtil.isNotBlank(clientId)) {
            dispatcherCommandCenter.dispatch(clientId, MqttMessageType.PUBCOMP, () -> {
                doPubComp(clientId, channel, message);
                return null;
            });
        }
    }

    /**
     * pub comp
     *
     * @param clientId
     * @param channel
     * @param message
     */
    private void doPubComp(String clientId, Channel channel, MqttMessage message) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader)message.variableHeader();
        int messageId = variableHeader.messageId();
        pubRelMessageStore.remove(clientId, messageId);

        String userName = NettyUtils.userName(channel);
        eventListenerExecutor.execute(new PubCompEventMessage(clientId, userName, messageId), IEventListener.Type.PUB_COMP);
    }
}
