package joey.mqtt.broker.provider;

import com.hazelcast.core.HazelcastInstance;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.hazelcast.HazelcastFactory;
import joey.mqtt.broker.innertraffic.HazelcastInnerTraffic;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.innertraffic.InnerPublishEventProcessor;
import joey.mqtt.broker.store.*;
import joey.mqtt.broker.store.hazelcast.*;
import lombok.extern.slf4j.Slf4j;

/**
 * hazelcast扩展实现
 *
 * @author Joey
 * @date 2020/04/09
 */
@Slf4j
public class HazelcastExtendProvider extends MemoryExtendProvider {
    private final String configFile;

    private final HazelcastInstance hzInstance;

    /**
     * 默认适配器 反射调用此构造方法
     *
     * @param customConfig
     */
    public HazelcastExtendProvider(CustomConfig customConfig) {
        super(customConfig);

        this.configFile = customConfig.getHazelcastConfigFile();
        this.hzInstance = HazelcastFactory.createInstance(this.configFile);
    }

    @Override
    public IMessageIdStore initMessageIdStore() {
        return new HazelcastMessageIdStore(hzInstance, customConfig);
    }

    @Override
    public ISubscriptionStore initSubscriptionStore(ISessionStore sessionStore) {
        return new HazelcastSubscriptionStore(hzInstance, customConfig);
    }

    @Override
    public IRetainMessageStore initRetainMessageStore() {
        return new HazelcastRetainMessageStore(hzInstance, customConfig);
    }

    @Override
    public IDupPubMessageStore initDupPubMessageStore() {
        return new HazelcastDupPubMessageStore(hzInstance, customConfig);
    }

    @Override
    public IDupPubRelMessageStore initDupPubRelMessageStore() {
        return new HazelcastDupPubRelMessageStore(hzInstance, customConfig);
    }

    @Override
    public IInnerTraffic initInnerTraffic(InnerPublishEventProcessor innerPublishEventProcessor, String nodeName) {
        return new HazelcastInnerTraffic(hzInstance, innerPublishEventProcessor, customConfig, nodeName);
    }
}
