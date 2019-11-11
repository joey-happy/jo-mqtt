package joey.mqtt.broker.store.memory;

import cn.hutool.core.collection.CollUtil;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.store.IRetainMessageStore;
import joey.mqtt.broker.util.TopicUtils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存retain消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class MemoryRetainMessageStore implements IRetainMessageStore {
    private ConcurrentHashMap<String, CommonPublishMessage> messageCache = new ConcurrentHashMap<>();

    public MemoryRetainMessageStore(CustomConfig customConfig) {

    }

    @Override
    public void add(CommonPublishMessage message) {
        messageCache.put(message.getTopic(), message);
    }

    @Override
    public void remove(String topic) {
        messageCache.remove(topic);
    }

    @Override
    public List<CommonPublishMessage> match(String topic) {
        List<CommonPublishMessage> retainMessageList = CollUtil.newLinkedList();

        List<String> subTokenList = TopicUtils.getTokenList(topic);
        if (CollUtil.isNotEmpty(subTokenList)) {
            ConcurrentHashMap.KeySetView<String, CommonPublishMessage> topicKeySet = messageCache.keySet();

            if (CollUtil.isNotEmpty(topicKeySet)) {
                Iterator<String> iterator = topicKeySet.iterator();

                while (iterator.hasNext()) {
                    String matchTopic = iterator.next();

                    if (TopicUtils.match(subTokenList, TopicUtils.getTokenList(matchTopic))) {
                        CommonPublishMessage retainMessage = messageCache.get(matchTopic);

                        if (null != retainMessage) {
                            retainMessageList.add(retainMessage);
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
