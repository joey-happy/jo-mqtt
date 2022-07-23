package joey.mqtt.broker.store.memory;

import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.store.IDupPubRelMessageStore;

/**
 * 内存pubRel消息存储
 *
 * @author Joey
 * @date 2019/7/23
 */
public class MemoryDupPubRelMessageStore extends MemoryDupBaseMessageStore implements IDupPubRelMessageStore {
    public MemoryDupPubRelMessageStore(CustomConfig customConfig) {
        super(customConfig);
    }
}
