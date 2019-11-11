package joey.mqtt.broker.event.processor;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.ConnectionLostEventMessage;
import joey.mqtt.broker.store.ISessionStore;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 连接事件丢失处理
 *
 * @author Joey
 * @date 2019/9/14
 */
@Slf4j
public class ConnectionLostEventProcessor implements IEventProcessor<MqttMessage> {
    private final ISessionStore sessionStore;

    private final PublishEventProcessor publishEventProcessor;

    private final EventListenerExecutor eventListenerExecutor;

    public ConnectionLostEventProcessor(ISessionStore sessionStore, PublishEventProcessor publishEventProcessor, EventListenerExecutor eventListenerExecutor) {
        this.sessionStore = sessionStore;
        this.publishEventProcessor = publishEventProcessor;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage message) {
        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);
        String userName = NettyUtils.userName(channel);

        if (StrUtil.isNotBlank(clientId)) {
            ClientSession clientSession = sessionStore.get(clientId);

            if (null != clientSession) {
                log.info("Process-connectionLost. clientId={},userName={}", clientId, userName);
                //session移除
                sessionStore.remove(clientId);

                MqttPublishMessage willMessage = clientSession.getWillMessage();
                //发送遗言消息
                if (null != willMessage) {
                    CommonPublishMessage willPubMsg = CommonPublishMessage.convert(willMessage, true);
                    log.info("Process-connectionLost publish will message. clientId={},userName={},topic={}", clientId, userName, willPubMsg.getTopic());

                    //发布遗言消息到订阅者
                    publishEventProcessor.publish2Subscribers(willPubMsg);

                    //存储retain遗言
                    publishEventProcessor.handleRetainMessage(willPubMsg);
                }

                //处理监听连接丢失事件
                eventListenerExecutor.execute(new ConnectionLostEventMessage(clientId, userName), IEventListener.Type.CONNECTION_LOST);
            }
        }
    }
}
