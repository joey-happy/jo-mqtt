package joey.mqtt.broker.store.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内存dup消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class MemoryDupBaseMessageStore {
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, CommonPublishMessage>> messageCache;

    /**
     * 并发操作锁map
     */
    private ConcurrentHashMap<String, AtomicBoolean> lockMap;

    private CustomConfig customConfig;

    public MemoryDupBaseMessageStore(CustomConfig customConfig) {
        this.messageCache = new ConcurrentHashMap<>();
        this.lockMap = new ConcurrentHashMap<>();
        this.customConfig = customConfig;
    }

    public void add(CommonPublishMessage message) {
        //解决添加和删除并发操作问题
        for (;;) {
            String clientId = message.getTargetClientId();
            AtomicBoolean lockFlag = lockMap.computeIfAbsent(clientId, b -> new AtomicBoolean(false));
            if (lockFlag.compareAndSet(false, true)) {
                ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.computeIfAbsent(clientId, m -> new ConcurrentHashMap<>());
                msgMap.put(message.getMessageId(), message);
                lockMap.remove(clientId);
                break;
            }
        }
    }

    public List<CommonPublishMessage> get(String clientId) {
        List<CommonPublishMessage> msgList = new ArrayList<>();

        ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.get(clientId);
        if (CollUtil.isNotEmpty(msgMap)) {
            msgList = CollUtil.newArrayList(msgMap.values());
        }

        return msgList;
    }

    public CommonPublishMessage get(String clientId, int messageId) {
        ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.get(clientId);
        if (CollUtil.isNotEmpty(msgMap)) {
            return msgMap.get(messageId);
        }

        return null;
    }

    public void remove(String clientId, int messageId) {
        //解决添加和删除并发操作问题
        for (;;) {
            AtomicBoolean lockFlag = lockMap.computeIfAbsent(clientId, b -> new AtomicBoolean(false));
            if (lockFlag.compareAndSet(false, true)) {
                ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.get(clientId);
                if (MapUtil.isNotEmpty(msgMap)) {
                    msgMap.remove(messageId);
                }

                lockMap.remove(clientId);
                break;
            }
        }
    }

    public void removeAllFor(String clientId) {
        //解决添加和删除并发操作问题
        for (;;) {
            AtomicBoolean lockFlag = lockMap.computeIfAbsent(clientId, b -> new AtomicBoolean(false));
            if (lockFlag.compareAndSet(false, true)) {
                messageCache.remove(clientId);
                lockMap.remove(clientId);
                break;
            }
        }
    }

    public void close() {

    }
}
