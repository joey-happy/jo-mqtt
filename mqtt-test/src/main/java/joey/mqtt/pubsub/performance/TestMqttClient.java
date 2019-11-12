package joey.mqtt.pubsub.performance;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 */
public class TestMqttClient extends Thread implements MqttCallback {
    private String topic;

    private String clientId;

    private String broker;

    private Integer qos;

    public TestMqttClient(String topic, String clientId, String broker, Integer qos) {
        this.topic = topic;
        this.clientId = clientId;
        this.broker = broker;
        this.qos = qos;
    }

    @Override
    public void run() {
        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setConnectionTimeout(3000);
//            connOpts.setWill("test/will1", "I am over!".getBytes(), MqttQoS.AT_MOST_ONCE.value(), false);
            connOpts.setCleanSession(false);

            connOpts.setUserName("local");
            connOpts.setPassword("local".toCharArray());
            client.setCallback(this);
            client.connect(connOpts);

            client.subscribe(topic, qos);
            boolean isSuccess = client.isConnected();
            System.out.println("conn status:" + isSuccess);

            if (isSuccess) {
                MqttCounter.addConnectCount();
            } else {
                MqttCounter.addConnectFailCount();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        MqttCounter.addConnectLostCount();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
//        System.out.println(new String(message.getPayload()));
        MqttCounter.addReceiveCount();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}