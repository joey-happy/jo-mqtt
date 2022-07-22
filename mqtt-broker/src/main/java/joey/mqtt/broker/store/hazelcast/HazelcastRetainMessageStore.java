package joey.mqtt.broker.store.hazelcast;

import cn.hutool.core.collection.CollUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.store.IRetainMessageStore;
import joey.mqtt.broker.util.TopicUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * hazelcast retain消息存储
 *
 * @author Joey
 * @date 2021/03/18
 */
public class HazelcastRetainMessageStore extends HazelcastBaseStore implements IRetainMessageStore {
    private final IMap<String, CommonPublishMessage> retainMsgMap;

    public HazelcastRetainMessageStore(HazelcastInstance hzInstance, CustomConfig customConfig) {
        super(hzInstance, customConfig);

        retainMsgMap = hzInstance.getMap(Constants.HAZELCAST_MSG_RETAIN);
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

        List<String> subTokenList = TopicUtils.getTokenList(topic);
        if (CollUtil.isNotEmpty(subTokenList)) {
            Collection<CommonPublishMessage> msgCollection = retainMsgMap.values();

            if (CollUtil.isNotEmpty(msgCollection)) {
                Iterator<CommonPublishMessage> iterator = msgCollection.iterator();

                while (iterator.hasNext()) {
                    CommonPublishMessage retainMessage = iterator.next();

                    if (TopicUtils.match(subTokenList, TopicUtils.getTokenList(retainMessage.getTopic()))) {
                        retainMessageList.add(retainMessage);
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
