package joey.mqtt.broker.core.subscription;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * topic订阅对象
 * @author Joey
 * @date 2019/7/22
 */
@Getter
public class Subscription implements Serializable {
    private final String clientId;

    private final String topic;

    private final MqttQoS qos;

    public Subscription(String clientId, String topic, MqttQoS qos) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscription that = (Subscription) o;
        return Objects.equals(clientId, that.clientId) && Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, topic);
    }

}
