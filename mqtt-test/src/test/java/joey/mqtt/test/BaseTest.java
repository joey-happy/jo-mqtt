package joey.mqtt.test;

import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.util.ConfigUtils;

import static joey.mqtt.broker.constant.BusinessConstants.MQTT_CONFIG;

/**
 * @author Joey
 * @date 2019/9/18
 */
public class BaseTest {
    protected String serviceUrl = "tcp://172.16.32.179:1883";

    protected String userName = "local";

    protected String password = "local";

    protected int connectionTimeout = 30;

    private static final String MQTT_CONF_FILE = "classpath:mqtt-conf.properties";

    protected Config getConfig() {
        System.setProperty(MQTT_CONFIG, MQTT_CONF_FILE);
        return ConfigUtils.loadFromSystemProps(MQTT_CONFIG, new Config());
    }
}
