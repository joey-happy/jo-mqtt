package joey.mqtt.broker.event.processor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * 处理事件接口定义
 *
 * @author Joey
 * @date 2019/7/22
 */
public interface IEventProcessor<T extends MqttMessage> {
    /**
     * 处理事件
     * @param ctx
     * @param message
     */
    void process(ChannelHandlerContext ctx, T message);
}
