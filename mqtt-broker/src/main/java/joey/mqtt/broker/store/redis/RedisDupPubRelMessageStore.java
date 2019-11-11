package joey.mqtt.broker.store.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.IDupPubRelMessageStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * redis pubRel消息存储
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisDupPubRelMessageStore implements IDupPubRelMessageStore {
    private final RedisClient redisClient;

    public RedisDupPubRelMessageStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    private String getRedisKey(String clientId) {
        return Constants.REDIS_MSG_DUP_PUB_REL_KEY_PRE + clientId;
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
    public CommonPublishMessage get(String clientId, int messageId) {
        String msgStr = redisClient.hget(getRedisKey(clientId), String.valueOf(messageId));

        if (StrUtil.isNotBlank(msgStr)) {
            return JSON.parseObject(msgStr, CommonPublishMessage.class);
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
