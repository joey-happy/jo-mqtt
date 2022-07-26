package joey.mqtt.broker.event.processor;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.ConnectionLostEventMessage;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.store.ISessionStore;
import joey.mqtt.broker.util.NettyUtils;
import joey.mqtt.broker.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 连接事件丢失处理
 *
 * @author Joey
 * @date 2019/9/14
 */
@Slf4j
public class ConnectionLostEventProcessor implements IEventProcessor<MqttMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final ISessionStore sessionStore;

    private final PublishEventProcessor publishEventProcessor;

    private final EventListenerExecutor eventListenerExecutor;

    private final IInnerTraffic innerTraffic;

    private final String nodeName;

    public ConnectionLostEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, ISessionStore sessionStore, PublishEventProcessor publishEventProcessor, IInnerTraffic innerTraffic, EventListenerExecutor eventListenerExecutor, String nodeName) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.sessionStore = sessionStore;
        this.publishEventProcessor = publishEventProcessor;
        this.eventListenerExecutor = eventListenerExecutor;
        this.innerTraffic = innerTraffic;
        this.nodeName = nodeName;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage message) {
        Channel channel = ctx.channel();
        String clientId = NettyUtils.clientId(channel);
        String userName = NettyUtils.userName(channel);

        if (StrUtil.isNotBlank(clientId)) {
            dispatcherCommandCenter.dispatch(clientId, "Connection-Lost", () -> {
                doConnectionLost(clientId, userName);
                return null;
            });
        }
    }

    /**
     * 断开连接
     *
     * @param clientId
     * @param userName
     */
    private void doConnectionLost(String clientId, String userName) {
        Optional.ofNullable(sessionStore.get(clientId))
                .ifPresent(clientSession -> {
                    log.info("Process-connectionLost. clientId={},userName={}", clientId, userName);

                    //发送遗言消息
                    Optional.ofNullable(clientSession.getPubMsgForWillMessage())
                            .ifPresent(willPubMsg -> {
                                willPubMsg.setSourceNodeName(nodeName);

                                Stopwatch stopwatch = Stopwatch.start();
                                log.info("Process-connectionLost publish will message. clientId={},userName={},topic={}", clientId, userName, willPubMsg.getTopic());

                                //集群间发送消息
                                try {
                                    innerTraffic.publish(willPubMsg);
                                    log.info("Process-connectionLost publish will message to cluster end. clientId={},userName={},topic={},timeCost={}ms", clientId, userName, willPubMsg.getTopic(), stopwatch.elapsedMills());
                                } catch (Exception ex) {
                                    log.error("Process-connectionLost publish will message with inner traffic error.", ex);
                                }

                                //发布遗言消息到订阅者
                                publishEventProcessor.publish2Subscribers(willPubMsg);

                                //存储retain遗言
                                publishEventProcessor.handleRetainMessage(willPubMsg);
                            });

                    //移除session
                    sessionStore.remove(clientId);

                    //处理监听连接丢失事件
                    eventListenerExecutor.execute(new ConnectionLostEventMessage(clientId, userName), IEventListener.Type.CONNECTION_LOST);
                });
    }
}
