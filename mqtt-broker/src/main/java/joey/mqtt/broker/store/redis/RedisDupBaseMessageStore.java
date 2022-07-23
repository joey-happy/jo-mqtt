package joey.mqtt.broker.store.redis;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.redis.RedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * redis dup消息存储
 *
 * @author Joey
 * @date 2019/9/7
 */
public abstract class RedisDupBaseMessageStore {
    private final RedisClient redisClient;

    public RedisDupBaseMessageStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    /**
     * 获取redis key
     *
     * @param clientId
     * @return
     */
    public abstract String getRedisKey(String clientId);

    public void add(CommonPublishMessage message) {
        redisClient.hset(getRedisKey(message.getTargetClientId()), String.valueOf(message.getMessageId()), JSON.toJSONString(message));
    }

    public List<CommonPublishMessage> get(String clientId) {
        List<CommonPublishMessage> msgList = new ArrayList<>();

        Optional.ofNullable(redisClient.hgetAllWithScan(getRedisKey(clientId), BusinessConstants.REDIS_EACH_SCAN_COUNT))
                .ifPresent(msgMap -> {
                    msgMap.forEach((msgId, msgStr) -> {
                        msgList.add(JSON.parseObject(msgStr, CommonPublishMessage.class));
                    });
                });

        return msgList;
    }

    public CommonPublishMessage get(String clientId, int messageId) {
        String msgJsonStr = redisClient.hget(getRedisKey(clientId), String.valueOf(messageId));
        if (StrUtil.isNotBlank(msgJsonStr)) {
            return JSON.parseObject(msgJsonStr, CommonPublishMessage.class);
        }

        return null;
    }

    public void remove(String clientId, int messageId) {
        redisClient.hdel(getRedisKey(clientId), String.valueOf(messageId));
    }

    public void removeAllFor(String clientId) {
        redisClient.del(getRedisKey(clientId));
    }

    public void close() {

    }
}
