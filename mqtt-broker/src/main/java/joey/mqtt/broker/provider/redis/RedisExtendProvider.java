package joey.mqtt.broker.provider.redis;

import cn.hutool.core.util.StrUtil;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.config.RedisConfig;
import joey.mqtt.broker.inner.IInnerTraffic;
import joey.mqtt.broker.inner.InnerPublishEventProcessor;
import joey.mqtt.broker.inner.redis.RedisInnerTraffic;
import joey.mqtt.broker.provider.adapter.ExtendProviderAdapter;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.IDupPubMessageStore;
import joey.mqtt.broker.store.IDupPubRelMessageStore;
import joey.mqtt.broker.store.IMessageIdStore;
import joey.mqtt.broker.store.IRetainMessageStore;
import joey.mqtt.broker.store.redis.RedisDupPubMessageStore;
import joey.mqtt.broker.store.redis.RedisDupPubRelMessageStore;
import joey.mqtt.broker.store.redis.RedisMessageIdStore;
import joey.mqtt.broker.store.redis.RedisRetainMessageStore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis 扩展实现
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisExtendProvider extends ExtendProviderAdapter {
    private RedisClient redisClient;

    private final RedisConfig redisConfig;

    /**
     * 反射调用此构造方法
     * @param customConfig
     */
    public RedisExtendProvider(CustomConfig customConfig) {
        super(customConfig);

        this.redisConfig = customConfig.getRedisConfig();

        initRedisClient();
    }

    private void initRedisClient() {
        JedisPoolConfig config = new JedisPoolConfig();
        //最大空闲连接数, 默认8个
        config.setMaxIdle(redisConfig.getPool().getMaxIdle());

        //最大连接数, 默认8个
        config.setMaxTotal(redisConfig.getPool().getMaxActive());

        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(redisConfig.getPool().getMaxWait());

        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTimeMillis(redisConfig.getPool().getMinEvictableIdleTimeMillis());

        //最小空闲连接数, 默认0
        config.setMinIdle(redisConfig.getPool().getMinIdle());

        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(redisConfig.getPool().isTestOnBorrow());

        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(redisConfig.getPool().isTestWhileIdle());

        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(redisConfig.getPool().getTimeBetweenEvictionRunsMillis());

        JedisPool jedisPool = null;
        String password = redisConfig.getPassword();
        if (StrUtil.isNotBlank(password)) {
            jedisPool = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(),
                                      redisConfig.getTimeout(), password, redisConfig.getDatabase());
        } else {
            jedisPool = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(),
                                      redisConfig.getTimeout(), null, redisConfig.getDatabase());
        }

        redisClient = new RedisClient(jedisPool);
    }

    @Override
    public IMessageIdStore initMessageIdStore() {
        return new RedisMessageIdStore(redisClient);
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
