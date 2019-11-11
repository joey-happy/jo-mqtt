package joey.mqtt.broker.store.redis;

import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.IMessageIdStore;

import static joey.mqtt.broker.Constants.*;

/**
 * redis 消息id存储
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisMessageIdStore implements IMessageIdStore {
    private final RedisClient redisClient;

    public RedisMessageIdStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public int getNextMessageId(String clientId) {
        String redisKey = REDIS_MSG_ID_KEY_PRE + clientId;

        Long incrId = redisClient.hincrBy(redisKey, REDIS_MSG_ID_FIELD, LONG_ONE);

        if (incrId >= Integer.MAX_VALUE) {
            redisClient.hset(redisKey, REDIS_MSG_ID_FIELD, String.valueOf(LONG_ZERO));
            incrId = redisClient.hincrBy(redisKey, REDIS_MSG_ID_FIELD, LONG_ONE);
        }

        for(;;) {
            int nextMsgId = (incrId.intValue() + INT_ONE) % 0xFFFF;
            if (INT_ZERO != nextMsgId) {
                return nextMsgId;
            }

            incrId = redisClient.hincrBy(redisKey, REDIS_MSG_ID_FIELD, LONG_ONE);
        }
    }
}
