package joey.mqtt.broker.event.message;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import joey.mqtt.broker.util.MessageUtils;
import lombok.Getter;

/**
 * 发布事件消息
 *
 * @author Joey
 * @date 2019/9/8
 */
public class PublishEventMessage extends AbstractEventMessage {
    private final MqttPublishMessage msg;

    @Getter
    private final String clientId;

    @Getter
    private final String userName;

    public PublishEventMessage(MqttPublishMessage msg, String clientId, String userName) {
        super(msg);
        this.msg = msg;
        this.clientId = clientId;
        this.userName = userName;
    }

    public String getTopic() {
        return msg.variableHeader().topicName();
    }

    public ByteBuf getPayload() {
        return msg.payload();
    }

    @Override
    public String info() {
        JSONObject obj = new JSONObject();
        obj.put("clientId", getClientId());
        obj.put("userName", getUserName());
        obj.put("topic", getTopic());
        obj.put("payload", new String(MessageUtils.readBytesAndRewind(getPayload())));

        return obj.toJSONString();
    }
}
