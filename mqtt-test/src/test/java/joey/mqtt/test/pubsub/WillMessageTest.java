package joey.mqtt.test.pubsub;

import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.test.BaseTest;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 遗言消息测试
 *
 * 参考：https://www.jianshu.com/p/65e1748a930c
 *
 * @author Joey
 * @date 2019/9/18
 */
public class WillMessageTest extends BaseTest {
    @Test
    public void testBaseWillMessageQos0() throws Exception {
        for (int i = 0; i<100000; i++) {
            MqttClient client = new MqttClient(serviceUrl, UUID.randomUUID().toString(), new MemoryPersistence());

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setConnectionTimeout(connectionTimeout);
            connOpts.setWill("test/will1", "I am over!".getBytes(), MqttQoS.AT_MOST_ONCE.value(), false);
            connOpts.setCleanSession(false);

            connOpts.setUserName(userName);
            connOpts.setPassword(password.toCharArray());

            client.connect(connOpts);
        }

        TimeUnit.SECONDS.sleep(120);
    }
}
