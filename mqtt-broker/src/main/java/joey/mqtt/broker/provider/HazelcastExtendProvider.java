package joey.mqtt.broker.provider;

import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.innertraffic.InnerPublishEventProcessor;
import joey.mqtt.broker.innertraffic.HazelcastInnerTraffic;

/**
 * hazelcast扩展实现
 *
 * @author Joey
 * @date 2020/04/09
 */
public class HazelcastExtendProvider extends ExtendProviderAdapter {

    /**
     * 默认适配器 反射调用此构造方法
     *
     * @param customConfig
     */
    public HazelcastExtendProvider(CustomConfig customConfig) {
        super(customConfig);
    }

    @Override
    public IInnerTraffic initInnerTraffic(InnerPublishEventProcessor innerPublishEventProcessor, String nodeName) {
        return new HazelcastInnerTraffic(innerPublishEventProcessor, customConfig, nodeName);
    }
}
