package joey.mqtt.broker.config;

import lombok.*;

/**
 * mqtt-broker-yaml文件配置
 *
 * @author Joey
 * @date 2019/7/18
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class YamlWrapperConfig {
    private Config mqtt = new Config();
}
