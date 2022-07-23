package joey.mqtt.broker.store.redis;

import cn.hutool.core.util.ObjectUtil;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.IMessageIdStore;

import static joey.mqtt.broker.constant.BusinessConstants.REDIS_MSG_ID_FIELD;
import static joey.mqtt.broker.constant.BusinessConstants.REDIS_MSG_ID_KEY_PRE;

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
        Long incrId = redisClient.hincrBy(redisKey, REDIS_MSG_ID_FIELD, NumConstants.LONG_1);

        if (incrId >= Integer.MAX_VALUE) {
            redisClient.hset(redisKey, REDIS_MSG_ID_FIELD, String.valueOf(NumConstants.LONG_0));
            incrId = redisClient.hincrBy(redisKey, REDIS_MSG_ID_FIELD, NumConstants.LONG_1);
        }

        for (;;) {
            int nextMsgId = (incrId.intValue() + NumConstants.INT_1) % 0xFFFF;
            if (ObjectUtil.notEqual(NumConstants.INT_0, nextMsgId)) {
                return nextMsgId;
            }

            incrId = redisClient.hincrBy(redisKey, REDIS_MSG_ID_FIELD, NumConstants.LONG_1);
        }
    }
}
