package joey.mqtt.broker.store.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.IDupPubMessageStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * redis pub消息存储
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisDupPubMessageStore implements IDupPubMessageStore {
    private final RedisClient redisClient;

    public RedisDupPubMessageStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    private String getRedisKey(String clientId) {
        return Constants.REDIS_MSG_DUP_PUB_KEY_PRE + clientId;
    }

    @Override
    public void add(CommonPublishMessage message) {
        redisClient.hset(getRedisKey(message.getTargetClientId()), String.valueOf(message.getMessageId()), JSON.toJSONString(message));
    }

    @Override
    public List<CommonPublishMessage> get(String clientId) {
        List<CommonPublishMessage> msgList = new ArrayList<>();

        Map<String, String> msgMap = redisClient.hgetAllWithScan(getRedisKey(clientId), Constants.REDIS_EACH_SCAN_COUNT);
        if (CollUtil.isNotEmpty(msgMap)) {
            Iterator<String> iterator = msgMap.values().iterator();

            while(iterator.hasNext()) {
                String msgStr = iterator.next();
                msgList.add(JSON.parseObject(msgStr, CommonPublishMessage.class));
            }
        }

        return msgList;
    }

    @Override
    public CommonPublishMessage get(String clientId, String messageId) {
        String msgJsonStr = redisClient.hget(getRedisKey(clientId), String.valueOf(messageId));
        if (StrUtil.isNotBlank(msgJsonStr)) {
            return JSON.parseObject(msgJsonStr, CommonPublishMessage.class);
        }

        return null;
    }

    @Override
    public void remove(String clientId, int messageId) {
        redisClient.hdel(getRedisKey(clientId), String.valueOf(messageId));
    }

    @Override
    public void removeAllFor(String clientId) {
        redisClient.del(getRedisKey(clientId));
    }

    @Override
    public void close() {

    }
}
