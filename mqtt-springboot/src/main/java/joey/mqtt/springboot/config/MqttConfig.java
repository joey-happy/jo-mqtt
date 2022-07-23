package joey.mqtt.springboot.config;

import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.config.NettyConfig;
import joey.mqtt.broker.config.ServerConfig;
import joey.mqtt.broker.constant.BusinessConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * mqtt相关配置
 *
 * @author Joey
 * @date 2019/9/7
 */
@Data
@Configuration
@ConfigurationProperties(prefix = BusinessConstants.MQTT_CONFIG_PROPS_PRE)
public class MqttConfig {
    private ServerConfig serverConfig;

    private NettyConfig nettyConfig;

    private CustomConfig customConfig;
}
