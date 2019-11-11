package joey.mqtt.broker.event.message;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Getter;

/**
 * 事件抽象消息
 *
 * @author Joey
 * @date 2019/9/8
 */
@Getter
public abstract class AbstractEventMessage implements EventMessage {
    private final boolean isRetain;

    private final boolean isDup;

    private final MqttQoS mqttQoS;

    protected AbstractEventMessage(MqttMessage msg) {
        MqttFixedHeader fixedHeader = msg.fixedHeader();
        this.isRetain = fixedHeader.isRetain();
        this.isDup = fixedHeader.isDup();
        this.mqttQoS = fixedHeader.qosLevel();
    }
}
