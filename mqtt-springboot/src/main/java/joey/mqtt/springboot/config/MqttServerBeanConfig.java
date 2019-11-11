package joey.mqtt.springboot.config;

import joey.mqtt.broker.Constants;
import joey.mqtt.broker.MqttServer;
import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Joey
 * @date 2019/9/9
 */
@Configuration
public class MqttServerBeanConfig {
    @Autowired
    private MqttConfig mqttConfig;

    @Bean
    public MqttServer mqttServer() throws Exception {
        //读取配置文件 优先级：命令行启动配置>jar包配置文件
        Config config = ConfigUtils.loadFromSystemProps(Constants.MQTT_CONFIG, new Config(mqttConfig.getServerConfig(), mqttConfig.getNettyConfig(), mqttConfig.getCustomConfig()));

        return new MqttServer(config);
    }
}
