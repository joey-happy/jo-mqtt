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
import joey.mqtt.broker.event.message.PubRecEventMessage;
import joey.mqtt.broker.store.IDupPubMessageStore;
import joey.mqtt.broker.store.IDupPubRelMessageStore;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * pubRec事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class PubRecEventProcessor implements IEventProcessor<MqttMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final IDupPubMessageStore dupPubMessageStore;

    private final IDupPubRelMessageStore dupPubRelMessageStore;

    private final EventListenerExecutor eventListenerExecutor;

    public PubRecEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, IDupPubMessageStore dupPubMessageStore, IDupPubRelMessageStore dupPubRelMessageStore, EventListenerExecutor eventListenerExecutor) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.dupPubMessageStore = dupPubMessageStore;
        this.dupPubRelMessageStore = dupPubRelMessageStore;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage message) {
        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);

        if (StrUtil.isNotBlank(clientId)) {
            dispatcherCommandCenter.dispatch(clientId, MqttMessageType.PUBREC, () -> {
                doPubRec(clientId, channel, message);
                return null;
            });
        }
    }

    /**
     * pub rec
     *
     * @param clientId
     * @param channel
     * @param message
     */
    private void doPubRec(String clientId, Channel channel, MqttMessage message) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader)message.variableHeader();
        int messageId = variableHeader.messageId();
        Optional.ofNullable(dupPubMessageStore.get(clientId, messageId))
                .ifPresent(pubMsg -> {
                    dupPubMessageStore.remove(clientId, messageId);
                    dupPubRelMessageStore.add(pubMsg.copy());
                });

        MqttMessage pubRelResp = MessageUtils.buildPubRelMessage(messageId, false);
        channel.writeAndFlush(pubRelResp);

        String userName = NettyUtils.userName(channel);
        eventListenerExecutor.execute(new PubRecEventMessage(clientId, userName, messageId), IEventListener.Type.PUB_REC);
    }
}
