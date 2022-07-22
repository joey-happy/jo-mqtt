package joey.mqtt.broker.store.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.store.IDupPubRelMessageStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内存pubRel消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class MemoryDupPubRelMessageStore implements IDupPubRelMessageStore {
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, CommonPublishMessage>> messageCache = new ConcurrentHashMap<>();

    /**
     * 并发操作锁map
     */
    private ConcurrentHashMap<String, AtomicBoolean> lockMap = new ConcurrentHashMap<>();

    public MemoryDupPubRelMessageStore(CustomConfig customConfig) {

    }

    @Override
    public void add(CommonPublishMessage message) {
        //解决添加和删除并发操作问题
        for(;;) {
            String clientId = message.getTargetClientId();
            lockMap.putIfAbsent(clientId, new AtomicBoolean(false));
            AtomicBoolean atomicBoolean = lockMap.get(clientId);

            if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.computeIfAbsent(clientId, m -> new ConcurrentHashMap<>());
                msgMap.put(message.getMessageId(), message);
                lockMap.remove(clientId);
                break;
            }
        }
    }

    @Override
    public List<CommonPublishMessage> get(String clientId) {
        List<CommonPublishMessage> msgList = new ArrayList<>();

        ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.get(clientId);
        if (CollUtil.isNotEmpty(msgMap)) {
            msgList = CollUtil.newArrayList(msgMap.values());
        }

        return msgList;
    }

    @Override
    public CommonPublishMessage get(String clientId, int messageId) {
        ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.get(clientId);
        if (CollUtil.isNotEmpty(msgMap)) {
            return msgMap.get(messageId);
        }

        return null;
    }

    @Override
    public void remove(String clientId, int messageId) {
        //解决添加和删除并发操作问题
        for(;;) {
            lockMap.putIfAbsent(clientId, new AtomicBoolean(false));
            AtomicBoolean atomicBoolean = lockMap.get(clientId);

            if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                ConcurrentHashMap<Integer, CommonPublishMessage> msgMap = messageCache.get(clientId);
                if (MapUtil.isNotEmpty(msgMap)) {
                    msgMap.remove(messageId);
                }

                lockMap.remove(clientId);
                break;
            }
        }
    }

    @Override
    public void removeAllFor(String clientId) {
        //解决添加和删除并发操作问题
        for(;;) {
            lockMap.putIfAbsent(clientId, new AtomicBoolean(false));
            AtomicBoolean atomicBoolean = lockMap.get(clientId);

            if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                messageCache.remove(clientId);
                lockMap.remove(clientId);
                break;
            }
        }
    }

    @Override
    public void close() {

    }

}
