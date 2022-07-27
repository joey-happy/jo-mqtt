package joey.mqtt.test.store.memory;

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
public class WillMessageForMemoryStoreTest extends BaseTest {
    private static final String WILL_TOPIC = "test/will";

    /**
     * 用客户端工具连接到服务器
     * 然后订阅遗言topic 在跑此用例即可
     *
     * @throws Exception
     */
    @Test
    public void testWillMessage() throws Exception {
        MqttClient client = new MqttClient(serviceUrl, UUID.randomUUID().toString(), new MemoryPersistence());

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setConnectionTimeout(connectionTimeout);
        connOpts.setWill(WILL_TOPIC, "I am over!".getBytes(), MqttQoS.AT_MOST_ONCE.value(), false);
        connOpts.setCleanSession(false);

        connOpts.setUserName(userName);
        connOpts.setPassword(password.toCharArray());

        client.connect(connOpts);

        TimeUnit.SECONDS.sleep(3);
    }

    /**
     * 用客户端工具连接到服务器
     * 然后订阅遗言topic 在跑此用例即可
     *
     * @throws Exception
     */
    @Test
    public void testWillMessageRetain() throws Exception {
        MqttClient client = new MqttClient(serviceUrl, UUID.randomUUID().toString(), new MemoryPersistence());

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setConnectionTimeout(connectionTimeout);
        connOpts.setWill(WILL_TOPIC, "I am over!".getBytes(), MqttQoS.AT_MOST_ONCE.value(), true);
        connOpts.setCleanSession(false);

        connOpts.setUserName(userName);
        connOpts.setPassword(password.toCharArray());

        client.connect(connOpts);

        TimeUnit.SECONDS.sleep(3);
    }
}
