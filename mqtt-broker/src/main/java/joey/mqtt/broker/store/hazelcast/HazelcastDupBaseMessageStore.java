package joey.mqtt.broker.store.hazelcast;

import cn.hutool.core.collection.CollUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * hazelcast dup消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class HazelcastDupBaseMessageStore extends HazelcastBaseStore {
    private final IMap<String, ConcurrentHashMap<Integer, CommonPublishMessage>> clientMsgMap;

    public HazelcastDupBaseMessageStore(HazelcastInstance hzInstance, CustomConfig customConfig, IMap<String, ConcurrentHashMap<Integer, CommonPublishMessage>> clientMsgMap) {
        super(hzInstance, customConfig);

        this.clientMsgMap = clientMsgMap;
    }

    public void add(CommonPublishMessage message) {
        String clientId = message.getTargetClientId();
        clientMsgMap.lock(clientId);

        try {
            ConcurrentHashMap<Integer, CommonPublishMessage> msgIdMap = clientMsgMap.computeIfAbsent(clientId, m -> new ConcurrentHashMap<>());
            msgIdMap.put(message.getMessageId(), message);
            clientMsgMap.put(clientId, msgIdMap);
        } finally {
            clientMsgMap.unlock(clientId);
        }
    }

    public List<CommonPublishMessage> get(String clientId) {
        List<CommonPublishMessage> msgList = new ArrayList<>();

        ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = clientMsgMap.get(clientId);
        if (CollUtil.isNotEmpty(msgMap)) {
            msgList = CollUtil.newArrayList(msgMap.values());
        }

        return msgList;
    }

    public CommonPublishMessage get(String clientId, int messageId) {
        return Optional.ofNullable(clientMsgMap.get(clientId))
                       .map(msgMap -> msgMap.get(messageId))
                       .orElse(null);
    }

    public void remove(String clientId, int messageId) {
        clientMsgMap.lock(clientId);

        try {
            Optional.ofNullable(clientMsgMap.get(clientId))
                    .ifPresent(msgIdMap -> {
                        msgIdMap.remove(messageId);
                    });
        } finally {
            clientMsgMap.unlock(clientId);
        }
    }

    public void removeAllFor(String clientId) {
        clientMsgMap.lock(clientId);

        try {
            clientMsgMap.remove(clientId);
        } finally {
            clientMsgMap.unlock(clientId);
        }
    }

    public void close() {

    }
}
