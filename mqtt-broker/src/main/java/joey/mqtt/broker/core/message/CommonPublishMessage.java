package joey.mqtt.broker.core.message;

import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import joey.mqtt.broker.util.MessageUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 通用发布消息
 *
 * @author Joey
 * @date 2019/9/16
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CommonPublishMessage implements Serializable {
    private String targetClientId;

    private String topic;

    private int messageId;

    private String messageBody;

    private int mqttQoS;

    private boolean isRetain = false;

    private boolean isWill = false;

    private long createTimestamp;

    public static CommonPublishMessage convert(MqttPublishMessage msg, boolean isWill) {
        CommonPublishMessage convert = new CommonPublishMessage();

        convert.topic = msg.variableHeader().topicName();
        convert.messageId = msg.variableHeader().packetId();
        convert.messageBody = new String(MessageUtils.readBytesAndRewind(msg.payload()));
        convert.mqttQoS = msg.fixedHeader().qosLevel().value();

        convert.isRetain = msg.fixedHeader().isRetain();
        convert.isWill = isWill;

        convert.createTimestamp = System.currentTimeMillis();

        return convert;
    }

    public CommonPublishMessage copy() {
        CommonPublishMessage copy = new CommonPublishMessage();

        copy.targetClientId = this.targetClientId;

        copy.topic = this.topic;
        copy.messageId = this.messageId;
        copy.messageBody = this.messageBody;
        copy.mqttQoS = this.mqttQoS;

        copy.isRetain = this.isRetain;
        copy.isWill = this.isWill;

        copy.createTimestamp = this.createTimestamp;

        return copy;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
