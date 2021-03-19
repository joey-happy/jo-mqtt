package joey.mqtt.broker.store.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.store.IMessageIdStore;

/**
 * hazelcast消息id存储
 *
 * 参考：https://www.jianshu.com/p/a015ffb2dd8f
 *
 * @author Joey
 * @date 2019/9/3
 */
public class HazelcastMessageIdStore extends HazelcastBaseStore implements IMessageIdStore {
    private final IMap<String, Integer> clientMsgIdMap;

    public HazelcastMessageIdStore(HazelcastInstance hzInstance, CustomConfig customConfig) {
        super(hzInstance, customConfig);

        clientMsgIdMap = hzInstance.getMap(Constants.HAZELCAST_MSG_ID);
    }

    @Override
    public int getNextMessageId(String clientId) {
        clientMsgIdMap.lock(clientId);
        try {
            Integer currentMsgId = clientMsgIdMap.get(clientId);
            if (null == currentMsgId) {
                currentMsgId = Constants.INT_ZERO;
            }

            Integer nextMsgId = (currentMsgId + Constants.INT_ONE) % 0xFFFF;
            if(Constants.INT_ZERO.equals(nextMsgId)) {
                nextMsgId = (nextMsgId + Constants.INT_ONE) % 0xFFFF;
            }

            clientMsgIdMap.put(clientId, nextMsgId);
            return nextMsgId;
        } finally {
            clientMsgIdMap.unlock(clientId);
        }
    }
}
