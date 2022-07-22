package joey.mqtt.pubsub.performance;

import cn.hutool.core.lang.Console;
import cn.hutool.system.SystemUtil;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Properties;

/**
 *
 */
public class TestMqttClient implements MqttCallback {
    private String topic;

    private String clientId;

    private String broker;

    private Integer qos;

    private boolean useSsl;

    public TestMqttClient(String topic, String clientId, String broker, Integer qos, boolean useSsl) {
        this.topic = topic;
        this.clientId = clientId;
        this.broker = broker;
        this.qos = qos;
        this.useSsl = useSsl;

        run();
    }

//    @Override
    public void run() {
        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setConnectionTimeout(3000);
//            connOpts.setWill("test/will1", "I am over!".getBytes(), MqttQoS.AT_MOST_ONCE.value(), false);
            connOpts.setCleanSession(false);

            connOpts.setUserName("local");
            connOpts.setPassword("local".toCharArray());

            if (useSsl) {
                Properties sslProperties = new Properties();

//                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, "/home/KeyStore.jks");
//                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, "123456");
//                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, "JKS");
//                sslProperties.put(SSLSocketFactoryFactory.CLIENTAUTH, true);

                String currentDir = SystemUtil.getUserInfo().getCurrentDir();
                String keyFilePath = currentDir + "/mqtt-test/src/main/resources/ssl/jomqtt-server.pfx";

//                System.setProperty("javax.net.ssl.trustStore", keyFilePath);
//                System.setProperty("javax.net.ssl.trustStorePassword", "jo_mqtt");
//                System.setProperty("javax.net.ssl.keyStore", keyFilePath);
//                System.setProperty("javax.net.ssl.keyStorePassword", "jo_mqtt");

                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, keyFilePath);
                sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, "jo_mqtt");
//                sslProperties.put(SSLSocketFactoryFactory.KEYSTORE, keyFilePath);
//                sslProperties.put(SSLSocketFactoryFactory.KEYSTOREPWD, "jo_mqtt");
//                sslProperties.put(SSLSocketFactoryFactory.KEYSTORETYPE, "PKCS12");

                connOpts.setHttpsHostnameVerificationEnabled(false);
                connOpts.setSSLProperties(sslProperties);
            }

            client.setCallback(this);
            client.connect(connOpts);

            client.subscribe(topic, qos);
            boolean isSuccess = client.isConnected();
            Console.log("conn status:" + isSuccess);

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
        Console.log(new String(message.getPayload()));
        MqttCounter.addReceiveCount();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}