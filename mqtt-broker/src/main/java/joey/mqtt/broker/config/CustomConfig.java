package joey.mqtt.broker.config;

import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 自定义配置
 * 用户可根据自身需求继承此类并自定义任何配置
 *
 * @author Joey
 * @date 2019/7/18
 */
@Getter
@Setter
@ToString
public class CustomConfig {
    /**
     * 若使用hazelcastExtendProvider集群间通信
     * 可配置hazelcast配置文件路径（不填将使用默认配置）
     */
    private String hazelcastConfigFile;

    /**
     * redis配置
     */
    private RedisConfig redisConfig = new RedisConfig();

    /**
     * sslContext配置
     */
    private SslContextConfig sslContextConfig = new SslContextConfig();

    /**
     * 节点名称 用于区分不同的服务实例
     */
    private String nodeName = IdUtil.fastSimpleUUID();
}
