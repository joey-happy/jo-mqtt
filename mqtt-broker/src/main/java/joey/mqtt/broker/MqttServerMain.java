package joey.mqtt.broker;

import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.util.ConfigUtils;
import joey.mqtt.broker.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;

/**
 * mqtt server 启动主方法
 *
 * @author Joey
 * @date 2019/7/18
 */
@Slf4j
public class MqttServerMain {
    public static void main(String[] args) throws Exception {
        Stopwatch start = Stopwatch.start();

        MqttServer server = new MqttServer(ConfigUtils.loadFromSystemProps(Constants.MQTT_CONFIG, new Config()));
        server.start();

        log.info("MqttServer-start. timeCost={}ms", start.elapsedMills());
    }
}
