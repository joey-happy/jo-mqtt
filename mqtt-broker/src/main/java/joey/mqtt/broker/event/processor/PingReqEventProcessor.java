package joey.mqtt.broker.event.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.PingEventMessage;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * ping响应事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class PingReqEventProcessor implements IEventProcessor<MqttMessage> {
    private final DispatcherCommandCenter dispatcherCommandCenter;

    private final EventListenerExecutor eventListenerExecutor;

    public PingReqEventProcessor(DispatcherCommandCenter dispatcherCommandCenter, EventListenerExecutor eventListenerExecutor) {
        this.dispatcherCommandCenter = dispatcherCommandCenter;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage message) {
        ctx.channel().writeAndFlush(MessageUtils.buildPingRespMessage());

        eventListenerExecutor.execute(new PingEventMessage(NettyUtils.clientId(ctx.channel()), NettyUtils.userName(ctx.channel())), IEventListener.Type.PING);
    }
}
