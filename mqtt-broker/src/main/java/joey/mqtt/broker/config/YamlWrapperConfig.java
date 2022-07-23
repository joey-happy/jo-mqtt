package joey.mqtt.broker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * mqtt-broker-yaml文件配置
 *
 * @author Joey
 * @date 2019/7/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YamlWrapperConfig {
    private Config mqtt = new Config();
}
