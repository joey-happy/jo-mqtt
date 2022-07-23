package joey.mqtt.broker.store.hazelcast;

import cn.hutool.core.util.ObjectUtil;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.constant.NumConstants;
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

        clientMsgIdMap = hzInstance.getMap(BusinessConstants.HAZELCAST_MSG_ID);
    }

    @Override
    public int getNextMessageId(String clientId) {
        clientMsgIdMap.lock(clientId);

        try {
            Integer currentMsgId = clientMsgIdMap.get(clientId);
            if (null == currentMsgId) {
                currentMsgId = NumConstants.INT_0;
            }

            Integer nextMsgId = (currentMsgId + NumConstants.INT_1) % 0xFFFF;
            if (ObjectUtil.equal(NumConstants.INT_0, nextMsgId)) {
                nextMsgId = (nextMsgId + NumConstants.INT_1) % 0xFFFF;
            }

            clientMsgIdMap.put(clientId, nextMsgId);
            return nextMsgId;
        } finally {
            clientMsgIdMap.unlock(clientId);
        }
    }
}
