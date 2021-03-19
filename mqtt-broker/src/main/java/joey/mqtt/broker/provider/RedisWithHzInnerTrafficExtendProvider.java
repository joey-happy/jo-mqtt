package joey.mqtt.broker.provider;

import com.hazelcast.core.HazelcastInstance;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.config.RedisConfig;
import joey.mqtt.broker.hazelcast.HazelcastFactory;
import joey.mqtt.broker.innertraffic.HazelcastInnerTraffic;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.innertraffic.InnerPublishEventProcessor;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.redis.RedisFactory;
import joey.mqtt.broker.store.*;
import joey.mqtt.broker.store.redis.*;

/**
 * redis与hazelcast混合实现
 *
 * redis实现qos所有等级要求 （有些redis集群不支持pub功能 例如:codis）
 * hazelcast实现集群间通信功能
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisWithHzInnerTrafficExtendProvider extends ExtendProviderAdapter {
    private RedisClient redisClient;

    private final RedisConfig redisConfig;

    private final String configFile;

    private final HazelcastInstance hzInstance;

    /**
     * 反射调用此构造方法
     * @param customConfig
     */
    public RedisWithHzInnerTrafficExtendProvider(CustomConfig customConfig) {
        super(customConfig);

        this.redisConfig = customConfig.getRedisConfig();
        this.redisClient = RedisFactory.createRedisClient(this.redisConfig);

        this.configFile = customConfig.getHazelcastConfigFile();
        this.hzInstance = HazelcastFactory.createInstance(this.configFile);
    }

    @Override
    public IMessageIdStore initMessageIdStore() {
        return new RedisMessageIdStore(redisClient);
    }

    @Override
    public ISubscriptionStore initSubscriptionStore(ISessionStore sessionStore) {
        return new RedisSubscriptionStore(redisClient, customConfig);
    }

    @Override
    public IRetainMessageStore initRetainMessageStore() {
        return new RedisRetainMessageStore(redisClient);
    }

    @Override
    public IDupPubMessageStore initDupPubMessageStore() {
        return new RedisDupPubMessageStore(redisClient);
    }

    @Override
    public IDupPubRelMessageStore initDupPubRelMessageStore() {
        return new RedisDupPubRelMessageStore(redisClient);
    }

    @Override
    public IInnerTraffic initInnerTraffic(InnerPublishEventProcessor innerPublishEventProcessor, String nodeName) {
        return new HazelcastInnerTraffic(hzInstance, innerPublishEventProcessor, customConfig, nodeName);
    }
}
