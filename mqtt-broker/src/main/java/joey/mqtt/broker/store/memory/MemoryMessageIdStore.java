package joey.mqtt.broker.store.memory;

import cn.hutool.core.util.ObjectUtil;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.store.IMessageIdStore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内存消息id存储
 *
 * @author Joey
 * @date 2019/9/3
 */
public class MemoryMessageIdStore implements IMessageIdStore {
    private final ConcurrentHashMap<String, Integer> clientMsgIdMap = new ConcurrentHashMap<>();

    /**
     * 并发操作锁map
     */
    private final ConcurrentHashMap<String, AtomicBoolean> lockMap = new ConcurrentHashMap<>();

    @Override
    public int getNextMessageId(String clientId) {
        //解决并发操作获取相同id问题
        for (;;) {
            lockMap.putIfAbsent(clientId, new AtomicBoolean(false));
            AtomicBoolean atomicBoolean = lockMap.get(clientId);

            if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                int currentMsgId = getCurrentMsgId(clientId);
                int nextMsgId = (currentMsgId + NumConstants.INT_1) % 0xFFFF;

                if (ObjectUtil.equals(NumConstants.INT_0, nextMsgId)) {
                    nextMsgId = (nextMsgId + NumConstants.INT_1) % 0xFFFF;
                }

                clientMsgIdMap.put(clientId, nextMsgId);
                lockMap.remove(clientId);

                return nextMsgId;
            }
        }
    }

    private Integer getCurrentMsgId(String clientId) {
        Integer currentMsgId = clientMsgIdMap.get(clientId);

        if (null == currentMsgId) {
            currentMsgId = NumConstants.INT_0;
            clientMsgIdMap.putIfAbsent(clientId, currentMsgId);
            currentMsgId = clientMsgIdMap.get(clientId);
        }

        return currentMsgId;
    }
}
