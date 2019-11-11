package joey.mqtt.broker.store.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.redis.RedisClient;
import joey.mqtt.broker.store.IRetainMessageStore;
import joey.mqtt.broker.util.TopicUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static joey.mqtt.broker.Constants.REDIS_EACH_SCAN_COUNT;
import static joey.mqtt.broker.Constants.REDIS_MSG_RETAIN_KEY;

/**
 * redis retain消息存储
 *
 * @author Joey
 * @date 2019/9/7
 */
public class RedisRetainMessageStore implements IRetainMessageStore {
    private final RedisClient redisClient;

    public RedisRetainMessageStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void add(CommonPublishMessage message) {
        redisClient.hset(REDIS_MSG_RETAIN_KEY, message.getTopic(), JSONObject.toJSONString(message));
    }

    @Override
    public void remove(String topic) {
        redisClient.hdel(REDIS_MSG_RETAIN_KEY, topic);
    }

    @Override
    public List<CommonPublishMessage> match(String topic) {
        List<CommonPublishMessage> retainMessageList = CollUtil.newLinkedList();
        List<String> subTokenList = TopicUtils.getTokenList(topic);

        if (CollUtil.isNotEmpty(subTokenList)) {
            Map<String, String> retainMessageMap = redisClient.hgetAllWithScan(REDIS_MSG_RETAIN_KEY, REDIS_EACH_SCAN_COUNT);
            Set<String> topicKeySet = retainMessageMap.keySet();

            if (CollUtil.isNotEmpty(topicKeySet)) {
                Iterator<String> iterator = topicKeySet.iterator();

                while (iterator.hasNext()) {
                    String matchTopic = iterator.next();

                    if (TopicUtils.match(subTokenList, TopicUtils.getTokenList(matchTopic))) {
                        String retainMessageStr = retainMessageMap.get(matchTopic);

                        if (StrUtil.isNotBlank(retainMessageStr)) {
                            retainMessageList.add(JSONObject.parseObject(retainMessageStr, CommonPublishMessage.class));
                        }
                    }
                }
            }
        }

        return retainMessageList;
    }

    @Override
    public void close() {

    }
}
