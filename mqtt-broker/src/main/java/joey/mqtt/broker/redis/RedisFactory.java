package joey.mqtt.broker.redis;

import cn.hutool.core.util.StrUtil;
import joey.mqtt.broker.config.RedisConfig;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * redis工厂
 * @author Joey
 * @date 2021-03-19
 */
public class RedisFactory {
    private RedisFactory() {

    }

    /**
     * 创建redis client
     *
     * @param redisConfig
     * @return
     */
    public static RedisClient createRedisClient(RedisConfig redisConfig) {
        JedisPoolConfig config = new JedisPoolConfig();

        // 最大空闲连接数, 默认8个
        RedisConfig.Pool pool = redisConfig.getPool();
        config.setMaxIdle(pool.getMaxIdle());

        // 最大连接数, 默认8个
        config.setMaxTotal(pool.getMaxActive());

        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWait(Duration.ofMillis(pool.getMaxWait()));

        // 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTime(Duration.ofMillis(pool.getMinEvictableIdleTimeMillis()));

        // 最小空闲连接数, 默认0
        config.setMinIdle(pool.getMinIdle());

        // 在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(pool.isTestOnBorrow());

        // 在空闲时检查有效性, 默认false
        config.setTestWhileIdle(pool.isTestWhileIdle());

        // 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRuns(Duration.ofMillis(pool.getTimeBetweenEvictionRunsMillis()));

        String password = StrUtil.trimToNull(redisConfig.getPassword());
        JedisPool jedisPool = new JedisPool(config, redisConfig.getHost(),
                                            redisConfig.getPort(),
                                            redisConfig.getTimeout(),
                                            password, redisConfig.getDatabase());

        return new RedisClient(jedisPool);
    }
}
