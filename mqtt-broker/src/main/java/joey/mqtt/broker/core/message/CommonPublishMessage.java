package joey.mqtt.broker.core.message;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import joey.mqtt.broker.util.MessageUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
    private String publishClientId;

    private String targetClientId;

    private String topic;

    private int messageId;

    private String messageBody;

    private int mqttQoS;

    private boolean isRetain;

    private boolean isDup;

    private boolean isWill;

    private String createTimeStr;

    private String sourceNodeName;

    /**
     * 转换消息
     *
     * @param publishClientId
     * @param msg
     * @param isWill
     * @param sourceNodeName
     * @return
     */
    public static CommonPublishMessage convert(String publishClientId, MqttPublishMessage msg, boolean isWill, String sourceNodeName) {
        CommonPublishMessage convert = new CommonPublishMessage();

        convert.publishClientId = publishClientId;

        convert.topic = msg.variableHeader().topicName();
        convert.messageId = msg.variableHeader().packetId();
        convert.messageBody = new String(MessageUtils.readBytesAndRewind(msg.payload()));
        convert.mqttQoS = msg.fixedHeader().qosLevel().value();
        convert.isDup = msg.fixedHeader().isDup();
        convert.isRetain = msg.fixedHeader().isRetain();
        convert.isWill = isWill;

        convert.createTimeStr = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
        convert.sourceNodeName = sourceNodeName;

        return convert;
    }

    /**
     * 拷贝消息
     *
     * @return
     */
    public CommonPublishMessage copy() {
        CommonPublishMessage copy = new CommonPublishMessage();

        copy.publishClientId = this.publishClientId;

        copy.topic = this.topic;
        copy.messageId = this.messageId;
        copy.messageBody = this.messageBody;
        copy.mqttQoS = this.mqttQoS;

        copy.isDup = this.isDup;
        copy.isRetain = this.isRetain;
        copy.isWill = this.isWill;

        copy.createTimeStr = this.createTimeStr;
        copy.sourceNodeName = this.sourceNodeName;

        return copy;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
