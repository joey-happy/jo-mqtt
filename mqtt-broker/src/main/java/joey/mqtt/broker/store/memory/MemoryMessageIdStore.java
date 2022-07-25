package joey.mqtt.broker.store.memory;

import cn.hutool.core.util.NumberUtil;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.store.IMessageIdStore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存消息id存储
 *
 * @author Joey
 * @date 2019/9/3
 */
public class MemoryMessageIdStore implements IMessageIdStore {
    private final ConcurrentHashMap<String, AtomicInteger> clientMsgIdMap = new ConcurrentHashMap<>();

    @Override
    public int getNextMessageId(String clientId) {
        AtomicInteger msgIdHolder = clientMsgIdMap.computeIfAbsent(clientId, m -> new AtomicInteger(NumConstants.INT_0));
        return msgIdHolder.updateAndGet(v -> NumberUtil.equals(v, NumConstants.INT_65535) ? NumConstants.INT_1 : (v + NumConstants.INT_1));
    }
}
