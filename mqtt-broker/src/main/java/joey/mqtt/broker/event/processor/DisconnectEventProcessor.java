package joey.mqtt.broker.event.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.DisconnectEventMessage;
import joey.mqtt.broker.store.IDupPubMessageStore;
import joey.mqtt.broker.store.IDupPubRelMessageStore;
import joey.mqtt.broker.store.ISessionStore;
import joey.mqtt.broker.store.ISubscriptionStore;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 断开连接事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class DisconnectEventProcessor implements IEventProcessor<MqttMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final ISessionStore sessionStore;

    private final ISubscriptionStore subStore;

    private final IDupPubMessageStore dupPubMessageStore;

    private final IDupPubRelMessageStore dupPubRelMessageStore;

    private final EventListenerExecutor eventListenerExecutor;

    public DisconnectEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, ISessionStore sessionStore, ISubscriptionStore subStore, IDupPubMessageStore dupPubMessageStore, IDupPubRelMessageStore dupPubRelMessageStore, EventListenerExecutor eventListenerExecutor) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.sessionStore = sessionStore;
        this.subStore = subStore;
        this.dupPubMessageStore = dupPubMessageStore;
        this.dupPubRelMessageStore = dupPubRelMessageStore;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage message) {
        Channel channel = ctx.channel();
        channel.flush();

        String clientId = NettyUtils.clientId(channel);
        String userName = NettyUtils.userName(channel);
        log.info("Process-disconnect. clientId={},userName={}", clientId, userName);

        ClientSession clientSession = sessionStore.get(clientId);
        if (null == clientSession) {
            channel.close();
            return;
        }

        if (!clientSession.isSameChannel(channel)) {
            log.warn("Process-disconnect. Another client is using the session. Closing connection. clientId={},userName={}", clientId, userName);
            clientSession.closeChannel();
            return;
        }

        if (clientSession.isCleanSession()) {
            subStore.removeAllBy(clientId);
            dupPubMessageStore.removeAllFor(clientId);
            dupPubRelMessageStore.removeAllFor(clientId);
        }

        clientSession.closeChannel();
        sessionStore.remove(clientId);

        eventListenerExecutor.execute(new DisconnectEventMessage(clientId, userName), IEventListener.Type.DISCONNECT);
    }
}
