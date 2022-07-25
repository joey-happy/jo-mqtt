package joey.mqtt.test.concurrent;

import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import joey.mqtt.test.BaseTest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @Author：Joey
 * @Date: 2022/7/24
 * @Desc:
 **/
public class ConcurrentTest extends BaseTest {

    @Slf4j
    @Data
    @AllArgsConstructor
    public static class ClientWorker implements Runnable {
        private CountDownLatch latch;

        private String serviceUrl;

        private String userName;

        private String password;

        @Override
        public void run() {
            Map<MqttClient, MqttConnectOptions> clientMap = MapUtil.newHashMap();

            for (int i = 0; i<10; i++) {
                MqttClient client = null;
                try {
                    client = new MqttClient(serviceUrl, IdUtil.simpleUUID(), new MemoryPersistence());
                } catch (MqttException e) {
                    log.error(StrUtil.EMPTY, e);
                }

                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setConnectionTimeout(1000000);
                connOpts.setCleanSession(true);

                connOpts.setUserName(userName);
                connOpts.setPassword(password.toCharArray());

                clientMap.put(client, connOpts);
            }

            try {
                latch.await();
                clientMap.forEach((client, connOpts) -> {
                    try {
                        client.connect(connOpts);
                    } catch (MqttException e) {
                        log.error(StrUtil.EMPTY, e);
                    }
                });
            } catch (InterruptedException e) {
                log.error(StrUtil.EMPTY, e);
            }
        }
    }

    @Test
    public void testConcurrentConnect() throws Exception {
        CountDownLatch mainLatch = new CountDownLatch(1);

        for (int i = 0; i<100; i++) {
            new Thread(new ClientWorker(mainLatch, serviceUrl, userName, password)).start();
        }

        mainLatch.countDown();
        Console.log("start");

        System.in.read();
    }
}
