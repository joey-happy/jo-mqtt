package joey.mqtt.broker.event.message;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.mqtt.MqttConnectMessage;

/**
 * 连接事件消息
 *
 * @author Joey
 * @date 2019/9/8
 */
public class ConnectEventMessage extends AbstractEventMessage {
    private final MqttConnectMessage msg;

    public ConnectEventMessage(MqttConnectMessage msg) {
        super(msg);
        this.msg = msg;
    }

    public String getClientId() {
        return msg.payload().clientIdentifier();
    }

    public boolean isCleanSession() {
        return msg.variableHeader().isCleanSession();
    }

    public int getKeepAlive() {
        return msg.variableHeader().keepAliveTimeSeconds();
    }

    public boolean isPasswordFlag() {
        return msg.variableHeader().hasPassword();
    }

    public byte getProtocolVersion() {
        return (byte) msg.variableHeader().version();
    }

    public String getProtocolName() {
        return msg.variableHeader().name();
    }

    public boolean isUserFlag() {
        return msg.variableHeader().hasUserName();
    }

    public boolean isWillFlag() {
        return msg.variableHeader().isWillFlag();
    }

    public byte getWillQos() {
        return (byte) msg.variableHeader().willQos();
    }

    public boolean isWillRetain() {
        return msg.variableHeader().isWillRetain();
    }

    public String getUserName() {
        return msg.payload().userName();
    }

    public byte[] getPassword() {
        return msg.payload().password().getBytes();
    }

    public String getWillTopic() {
        return msg.payload().willTopic();
    }

    public byte[] getWillMessage() {
        return msg.payload().willMessage().getBytes();
    }

    @Override
    public String info() {
        JSONObject obj = new JSONObject();
        obj.put("clientId", getClientId());
        obj.put("userName", getUserName());
        return obj.toJSONString();
    }
}
