package joey.mqtt.broker.store.redis;

import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.IDupPubMessageStore;

/**
 * redis pub消息存储
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisDupPubMessageStore extends RedisDupBaseMessageStore implements IDupPubMessageStore {
    public RedisDupPubMessageStore(RedisClient redisClient) {
        super(redisClient);
    }

    @Override
    public String getRedisKey(String clientId) {
        return BusinessConstants.REDIS_MSG_DUP_PUB_KEY_PRE + clientId;
    }
}
