/**
 * Copyright (c) 2014 http://www.jieqianhua.com
 */
package joey.mqtt.broker.redis;

import cn.hutool.core.collection.CollUtil;
import redis.clients.jedis.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * redis客户端
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisClient {
    private static final String OK_STR = "OK";

    private JedisPool jedisPool;

    public RedisClient() {
    }

    public RedisClient(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public void setex(String key, String value, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, seconds, value);
        }
    }

    public long incr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }

    public long incrBy(String key, long num) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, num);
        }
    }

    public String ping() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ping();
        }
    }

    public Long setnx(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setnx(key, value);
        }
    }

    public String setex(String key, int seconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setex(key, seconds, value);
        }
    }

    public long expire(String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds);
        }
    }

    public String getSet(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getSet(key, value);
        }
    }

    public long del(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        }
    }

    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public Map<String, String> hgetAllWithScan(String key, int eachScanCount) {
        Map<String, String> resultMap = new HashMap<>();

        try (Jedis jedis = jedisPool.getResource()) {
            int cursor = 0;

            ScanParams scanParams = new ScanParams();
            scanParams.count(eachScanCount);

            do {
                ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(key, String.valueOf(cursor), scanParams);
                List<Map.Entry<String, String>> entryList = scanResult.getResult();

                if (CollUtil.isNotEmpty(entryList)) {
                    entryList.forEach(entry -> {
                        resultMap.put(entry.getKey(), entry.getValue());
                    });
                }

                cursor = Integer.parseInt(scanResult.getCursor());
            } while (cursor > 0);
        }

        return resultMap;
    }

    public Map<String, String> hgetAll(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }

    public long hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, field, value);
        }
    }

    public String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    public boolean hexist(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists(key, field);
        }
    }

    public boolean hmset(String key, Map<String, String> hash) {
        try (Jedis jedis = jedisPool.getResource()) {
            return OK_STR.equals(jedis.hmset(key, hash));
        }
    }

    public List<String> hmget(String key, String... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmget(key, fields);
        }
    }

    public long hdel(String key, String... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hdel(key, fields);
        }
    }

    public List<String> hvals(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hvals(key);
        }
    }

    public Long hincrBy(String key, String field, long value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrBy(key, field, value);
        }
    }

    public Long ttl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        }
    }

    public Long pfadd(String key, String... elements) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfadd(key, elements);
        }
    }

    public Long pfcount(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pfcount(key);
        }
    }

    public Long sadd(String key, String... members) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sadd(key, members);
        }
    }

    public Long scard(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scard(key);
        }
    }

    public Boolean sismember(String key, String member) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sismember(key, member);
        }
    }

    public Long srem(String key, String member){
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.srem(key, member);
        }
    }

    public void publish(String channel, String message){
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public void subscribe(JedisPubSub pubSub, String... channels){
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(pubSub, channels);
        }
    }
}
