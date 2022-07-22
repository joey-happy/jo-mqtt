package joey.mqtt.broker.store.hazelcast;

import cn.hutool.core.collection.CollUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.store.IDupPubMessageStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * hazelcast dup pub消息存储
 *
 * @author Joey
 * @date 2021/03/18
 */
public class HazelcastDupPubMessageStore extends HazelcastBaseStore implements IDupPubMessageStore {
    private final IMap<String, ConcurrentHashMap<Integer, CommonPublishMessage>> clientMsgMap;

    public HazelcastDupPubMessageStore(HazelcastInstance hzInstance, CustomConfig customConfig) {
        super(hzInstance, customConfig);

        clientMsgMap = hzInstance.getMap(Constants.HAZELCAST_MSG_DUP_PUB);
    }


    @Override
    public void add(CommonPublishMessage message) {
        String clientId = message.getTargetClientId();
        clientMsgMap.lock(clientId);
        try {
            ConcurrentHashMap<Integer, CommonPublishMessage> msgIdMap = clientMsgMap.get(clientId);
            if (null == msgIdMap) {
                msgIdMap = new ConcurrentHashMap<>();
            }

            msgIdMap.put(message.getMessageId(), message);
            clientMsgMap.put(clientId, msgIdMap);
        } finally {
            clientMsgMap.unlock(clientId);
        }
    }

    @Override
    public List<CommonPublishMessage> get(String clientId) {
        List<CommonPublishMessage> msgList = new ArrayList<>();

        ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = clientMsgMap.get(clientId);
        if (CollUtil.isNotEmpty(msgMap)) {
            msgList = CollUtil.newArrayList(msgMap.values());
        }

        return msgList;
    }

    @Override
    public CommonPublishMessage get(String clientId, String messageId) {
        ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = clientMsgMap.get(clientId);
        if (CollUtil.isNotEmpty(msgMap)) {
            return msgMap.get(messageId);
        }

        return null;
    }

    @Override
    public void remove(String clientId, int messageId) {
        clientMsgMap.lock(clientId);
        try {
            ConcurrentHashMap<Integer, CommonPublishMessage> msgIdMap = clientMsgMap.get(clientId);
            if (null != msgIdMap) {
                msgIdMap.remove(messageId);
            }

            clientMsgMap.put(clientId, msgIdMap);
        } finally {
            clientMsgMap.unlock(clientId);
        }
    }

    @Override
    public void removeAllFor(String clientId) {
        clientMsgMap.lock(clientId);
        try {
            clientMsgMap.remove(clientId);
        } finally {
            clientMsgMap.unlock(clientId);
        }
    }

    @Override
    public void close() {

    }
}
