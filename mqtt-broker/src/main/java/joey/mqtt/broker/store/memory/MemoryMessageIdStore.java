package joey.mqtt.broker.store.memory;

import joey.mqtt.broker.store.IMessageIdStore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static joey.mqtt.broker.Constants.INT_ONE;
import static joey.mqtt.broker.Constants.INT_ZERO;

/**
 * 内存消息id存储
 *
 * @author Joey
 * @date 2019/9/3
 */
public class MemoryMessageIdStore implements IMessageIdStore {
    private ConcurrentHashMap<String, Integer> clientMsgIdMap = new ConcurrentHashMap<>();

    /**
     * 并发操作锁map
     */
    private ConcurrentHashMap<String, AtomicBoolean> lockMap = new ConcurrentHashMap<>();

    @Override
    public int getNextMessageId(String clientId) {
        //解决并发操作获取相同id问题
        for(;;) {
            lockMap.putIfAbsent(clientId, new AtomicBoolean(false));
            AtomicBoolean atomicBoolean = lockMap.get(clientId);

            if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                Integer currentMsgId = getCurrentMsgId(clientId);
                Integer nextMsgId = (currentMsgId + INT_ONE) % 0xFFFF;

                if(INT_ZERO.equals(nextMsgId)) {
                    nextMsgId = (nextMsgId + INT_ONE) % 0xFFFF;
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
            currentMsgId = INT_ZERO;
            clientMsgIdMap.putIfAbsent(clientId, currentMsgId);
            currentMsgId = clientMsgIdMap.get(clientId);
        }

        return currentMsgId;
    }
}
