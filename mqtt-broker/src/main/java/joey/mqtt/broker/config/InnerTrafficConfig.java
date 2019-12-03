package joey.mqtt.broker.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 集群间通信配置
 * @author Joey
 * @date 2019/12/3
 */
@Getter
@Setter
@ToString
public class InnerTrafficConfig {
    /**
     * 是否使用hazelcast作为集群间通信工具
     */
    private boolean enableHazelcast;

    /**
     * 若使用hazelcast作为集群间通信工具
     * 配置hazelcast配置文件路径（不填将使用默认配置）
     */
    private String hazelcastConfigFile;
}
