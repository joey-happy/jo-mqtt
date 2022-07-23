package joey.mqtt.broker.config;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义配置
 *
 * 用户可以根据自身需求配置extConf
 * 参考:test/resource/config.properties
 *
 * @author Joey
 * @date 2019/7/18
 */
@Data
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

    /**
     * 用户自定义扩展配置map
     */
    private Map<String, Object> extConfig = new HashMap<>();

    /**
     * 将自定义扩展配置 转换成用户定义的对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T convertExtConfig(Class<T> clazz) {
        return JSONObject.parseObject(JSON.toJSONString(extConfig), clazz);
    }
}
