package joey.mqtt.broker.provider;

import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.config.RedisConfig;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.innertraffic.InnerPublishEventProcessor;
import joey.mqtt.broker.innertraffic.RedisInnerTraffic;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.redis.RedisFactory;
import joey.mqtt.broker.store.*;
import joey.mqtt.broker.store.redis.*;

/**
 * redis 扩展实现
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisExtendProvider extends ExtendProviderAdapter {
    private final RedisClient redisClient;

    private final RedisConfig redisConfig;

    /**
     * 反射调用此构造方法
     * @param customConfig
     */
    public RedisExtendProvider(CustomConfig customConfig) {
        super(customConfig);

        this.redisConfig = customConfig.getRedisConfig();
        this.redisClient = RedisFactory.createRedisClient(this.redisConfig);
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
        return new RedisInnerTraffic(redisClient, innerPublishEventProcessor, nodeName);
    }

    @Override
    public String getNodeName() {
        return super.getNodeName();
    }
}
