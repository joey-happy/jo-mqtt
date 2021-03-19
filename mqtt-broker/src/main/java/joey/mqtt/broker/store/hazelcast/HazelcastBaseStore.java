package joey.mqtt.broker.store.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import joey.mqtt.broker.config.CustomConfig;

/**
 * hazelcast 基础存储
 *
 * @author Joey
 * @date 2021/03/18
 */
public class HazelcastBaseStore {
    protected final HazelcastInstance hzInstance;

    protected final CustomConfig customConfig;

    protected HazelcastBaseStore(HazelcastInstance hzInstance, CustomConfig customConfig) {
        this.hzInstance = hzInstance;
        this.customConfig = customConfig;
    }
}
