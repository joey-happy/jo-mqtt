package joey.mqtt.springboot.config;

import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.config.NettyConfig;
import joey.mqtt.broker.config.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * mqtt相关配置
 *
 * @author Joey
 * @date 2019/9/7
 */
@Setter
@Getter
@ToString
@Configuration
@ConfigurationProperties(prefix = Constants.MQTT_CONFIG_PROPS_PRE)
public class MqttConfig {
    private ServerConfig serverConfig;

    private NettyConfig nettyConfig;

    private CustomConfig customConfig;
}
