package joey.mqtt.broker.store.memory;

import cn.hutool.core.collection.CollUtil;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.store.IRetainMessageStore;
import joey.mqtt.broker.util.TopicUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存retain消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class MemoryRetainMessageStore implements IRetainMessageStore {
    private ConcurrentHashMap<String, CommonPublishMessage> retainMsgMap = new ConcurrentHashMap<>();

    public MemoryRetainMessageStore(CustomConfig customConfig) {

    }

    @Override
    public void add(CommonPublishMessage message) {
        retainMsgMap.put(message.getTopic(), message);
    }

    @Override
    public void remove(String topic) {
        retainMsgMap.remove(topic);
    }

    @Override
    public List<CommonPublishMessage> match(String topic) {
        List<CommonPublishMessage> retainMessageList = CollUtil.newLinkedList();

        List<String> subTokenList = TopicUtils.getTopicTokenList(topic);
        if (CollUtil.isNotEmpty(subTokenList)) {
            Optional.ofNullable(retainMsgMap.values())
                    .ifPresent(msgCollection -> {
                        msgCollection.forEach(retainMessage -> {
                            if (TopicUtils.match(subTokenList, TopicUtils.getTopicTokenList(retainMessage.getTopic()))) {
                                retainMessageList.add(retainMessage);
                            }
                        });
                    });
        }

        return retainMessageList;
    }

    @Override
    public void close() {

    }
}
