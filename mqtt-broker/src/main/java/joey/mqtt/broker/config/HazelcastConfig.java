package joey.mqtt.broker.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * hazelcast配置
 *
 * @author Joey
 * @date 2019/8/31
 */
@Getter
@Setter
@ToString
public class HazelcastConfig {
    /**
     * 是否启用
     */
    private boolean enable = true;

    /**
     * 配置文件位置
     */
    private String configFilePath;
}
