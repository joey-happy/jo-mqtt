package joey.mqtt.test;

import cn.hutool.core.util.IdUtil;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * @author Joey
 * @date 2019/9/18
 * @desc 基础测试类
 */
public class BaseTest {
    protected String serviceUrl = "tcp://localhost:1883";

    protected String userName = "local";

    protected String password = "local";

    protected int connectionTimeout = 30;

    /**
     * 构建mqtt client
     *
     * @param cleanSession
     * @return
     * @throws MqttException
     */
    protected MqttClient buildMqttClient(boolean cleanSession) throws MqttException {
        MqttClient client = new MqttClient(serviceUrl, IdUtil.simpleUUID(), new MemoryPersistence());

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setConnectionTimeout(connectionTimeout);
        connOpts.setCleanSession(cleanSession);

        connOpts.setUserName(userName);
        connOpts.setPassword(password.toCharArray());

        client.connect(connOpts);

        return client;
    }
}
