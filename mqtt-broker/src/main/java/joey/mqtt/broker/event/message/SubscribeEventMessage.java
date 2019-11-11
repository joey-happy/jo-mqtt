package joey.mqtt.broker.event.message;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.broker.core.subscription.Subscription;
import lombok.Getter;

/**
 * 订阅事件消息
 *
 * @author Joey
 * @date 2019/9/8
 */
@Getter
public class SubscribeEventMessage implements EventMessage {
    private final String clientId;

    private final String userName;

    private final String topic;

    private final MqttQoS mqttQos;

    public SubscribeEventMessage(Subscription subscription, String userName) {
        this.clientId = subscription.getClientId();
        this.mqttQos = subscription.getQos();
        this.topic = subscription.getTopic();
        this.userName = userName;
    }

    @Override
    public String info() {
        return JSONObject.toJSONString(this);
    }
}
