package joey.mqtt.springboot.runner;

import joey.mqtt.broker.MqttServer;
import joey.mqtt.broker.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * mqtt-server 启动
 *
 * @author Joey
 * @date 2019/8/28
 */
@Component
@Slf4j
public class MqttServerRunner implements CommandLineRunner {
    @Autowired
    private MqttServer mqttServer;

    @Override
    public void run(String... args) throws Exception {
        Stopwatch start = Stopwatch.start();

        try {
            mqttServer.start();
        } catch (Throwable t) {
            log.error("MqttServerRunner start error.", t);
        }

        log.info("MqttServer-start. timeCost={}ms", start.elapsedMills());
    }
}
