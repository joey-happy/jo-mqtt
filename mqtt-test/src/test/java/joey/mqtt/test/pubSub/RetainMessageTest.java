package joey.mqtt.test.pubSub;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.test.BaseTest;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 保留消息测试
 *
 * @author Joey
 * @date 2019/9/18
 */
public class RetainMessageTest extends BaseTest {
    private static final String RETAIN_TOPIC = "test/retain";

    /**
     *
     * @throws Exception
     */
    @Test
    public void testRetainMessage() throws Exception {
        final List<Boolean> invokeResult = CollUtil.newArrayList();

        MqttClient client = buildMqttClient(true);

        MqttMessage message = new MqttMessage();
        message.setQos(MqttQoS.AT_MOST_ONCE.value());
        message.setRetained(true);
        message.setPayload(IdUtil.simpleUUID().getBytes(StandardCharsets.UTF_8));
        client.publish(RETAIN_TOPIC, message);

        client.disconnect();

        client = buildMqttClient(true);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                invokeResult.add(ObjectUtil.equal(RETAIN_TOPIC, topic));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        client.subscribe(RETAIN_TOPIC);

        message = new MqttMessage();
        message.setQos(MqttQoS.AT_MOST_ONCE.value());
        message.setRetained(true);
        message.setPayload(StrUtil.EMPTY.getBytes(StandardCharsets.UTF_8));
        client.publish(RETAIN_TOPIC, message);

        MqttClient client1 = buildMqttClient(true);
        client1.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                invokeResult.add(false);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        client1.subscribe(RETAIN_TOPIC);

        invokeResult.stream().forEach(result -> {
            Assert.assertTrue(result);
        });
    }
}
