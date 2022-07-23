package joey.mqtt.broker.store.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.store.IDupPubMessageStore;

/**
 * hazelcast dup pub消息存储
 *
 * @author Joey
 * @date 2021/03/18
 */
public class HazelcastDupPubMessageStore extends HazelcastDupBaseMessageStore implements IDupPubMessageStore {
    public HazelcastDupPubMessageStore(HazelcastInstance hzInstance, CustomConfig customConfig) {
        super(hzInstance, customConfig, hzInstance.getMap(BusinessConstants.HAZELCAST_MSG_DUP_PUB));
    }
}
