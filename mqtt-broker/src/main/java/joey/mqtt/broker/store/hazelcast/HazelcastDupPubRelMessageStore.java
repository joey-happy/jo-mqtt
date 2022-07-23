package joey.mqtt.broker.store.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.store.IDupPubRelMessageStore;

/**
 * hazelcast dup pub rel消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class HazelcastDupPubRelMessageStore extends HazelcastDupBaseMessageStore implements IDupPubRelMessageStore {
    public HazelcastDupPubRelMessageStore(HazelcastInstance hzInstance, CustomConfig customConfig) {
        super(hzInstance, customConfig, hzInstance.getMap(BusinessConstants.HAZELCAST_MSG_DUP_PUB_REL));
    }
}
