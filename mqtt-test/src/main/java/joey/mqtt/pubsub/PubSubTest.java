package joey.mqtt.pubsub;

import cn.hutool.core.util.RandomUtil;
import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.pubsub.performance.MqttCounter;
import joey.mqtt.pubsub.performance.TestMqttClient;

import java.util.concurrent.TimeUnit;

/**
 * @author Joey
 * @date 2019/11/12
 */
public class PubSubTest {
    private static final String SERVICE_URL = "tcp://localhost:1883";
    private static final String DEFAULT_TOPIC = "jo/test";

    public static void main(String[] args) throws Exception {
        String broker;
        String topic;

        if (args.length >= 4) {
            broker = args[0];
            topic = args[1];
        } else {
            broker = SERVICE_URL;
            topic = DEFAULT_TOPIC;
        }

        String clientIdPre = RandomUtil.randomString(10);
        System.out.println("broker = " + broker + " topic = " + topic + " clientIdPre=" + clientIdPre);

        for (int i = 0; i < 5; i++) {
            new TestMqttClient(topic, clientIdPre + "_"+ i, broker, MqttQoS.AT_MOST_ONCE.value()).start();
        }

        //定时查询统计数量
        while (true) {
            System.out.println(MqttCounter.print());
            TimeUnit.SECONDS.sleep(2);
        }
    }
}
