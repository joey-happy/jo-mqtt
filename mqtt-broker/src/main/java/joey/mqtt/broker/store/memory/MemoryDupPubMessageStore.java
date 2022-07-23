package joey.mqtt.broker.store.memory;

import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.store.IDupPubMessageStore;

/**
 * 内存pub消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class MemoryDupPubMessageStore extends MemoryDupBaseMessageStore implements IDupPubMessageStore {
    public MemoryDupPubMessageStore(CustomConfig customConfig) {
        super(customConfig);
    }

}
