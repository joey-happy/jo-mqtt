package joey.mqtt.broker.store.redis;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.BaseSubscriptionStore;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * redis订阅存储
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class RedisSubscriptionStore extends BaseSubscriptionStore {
    private final RedisClient redisClient;

    public RedisSubscriptionStore(RedisClient redisClient, CustomConfig customConfig) {
        super(customConfig);
        this.redisClient = redisClient;
    }

    private String getRedisKey(Subscription subscription) {
        String clientId = subscription.getClientId();
        return getRedisKey(clientId);
    }

    private String getRedisKey(String clientId) {
        return Constants.REDIS_SUB_STORE_KEY + clientId;
    }

    @Override
    public boolean add(Subscription subscription, boolean onlyMemory) {
        boolean addResult = super.add(subscription);
        if(addResult && !onlyMemory) {
            redisClient.hset(getRedisKey(subscription), subscription.getTopic(), JSON.toJSONString(subscription));
            return true;
        }

        return addResult;
    }

    @Override
    public boolean remove(Subscription subscription) {
        boolean removeResult = super.remove(subscription);
        if(removeResult) {
            redisClient.hdel(getRedisKey(subscription), subscription.getTopic());
            return true;
        }

        return false;
    }

    @Override
    public Set<Subscription> findAllBy(String clientId) {
        Set<Subscription> subSet = new HashSet<>();
        List<String> subJsonStrList = redisClient.hvals(getRedisKey(clientId));

        if (CollUtil.isNotEmpty(subJsonStrList)) {
            for (String subJson : subJsonStrList) {
                subSet.add(JSON.parseObject(subJson, Subscription.class));
            }
        }

        return subSet;
    }

    @Override
    public void removeAllBy(String clientId) {
        Set<Subscription> subSet = findAllBy(clientId);

        if (CollUtil.isNotEmpty(subSet)) {
            Iterator<Subscription> iterator = subSet.iterator();

            //删除订阅关系
            while (iterator.hasNext()) {
                super.remove(iterator.next());
            }
        }

        redisClient.del(getRedisKey(clientId));
    }
}
