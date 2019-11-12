package joey.mqtt.pubsub;

import cn.hutool.core.util.RandomUtil;
import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.pubsub.performance.MqttCounter;
import joey.mqtt.pubsub.performance.TestMqttClient;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Joey
 * @date 2019/11/12
 */
public class PubSubTest {
    private static final String SERVICE_URL = "tcp://172.16.32.179:1883";
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

//        ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 300, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        for (int i = 0; i < 4500; i++) {
//            executor.execute(new TestMqttClient(topic, clientIdPre + "_"+ i, broker, MqttQoS.AT_MOST_ONCE.value()));
//            new Thread(new TestMqttClient(topic, clientIdPre + "_"+ i, broker, MqttQoS.AT_MOST_ONCE.value())).start();
            new TestMqttClient(topic, clientIdPre + "_"+ i, broker, MqttQoS.AT_MOST_ONCE.value());
        }

        //定时查询统计数量
        while (true) {
            System.out.println(MqttCounter.print());
            TimeUnit.SECONDS.sleep(2);
        }
    }
}
