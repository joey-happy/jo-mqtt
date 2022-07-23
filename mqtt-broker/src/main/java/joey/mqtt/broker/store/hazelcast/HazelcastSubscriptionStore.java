package joey.mqtt.broker.store.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.multimap.MultiMap;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.store.BaseSubscriptionStore;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * hazelcast订阅存储
 *
 * @author Joey
 * @date 2021/03/18
 */
@Slf4j
public class HazelcastSubscriptionStore extends BaseSubscriptionStore {
    private final HazelcastInstance hzInstance;

    private final CustomConfig customConfig;

    private final MultiMap<String, Subscription> clientSubMultiMap;

    public HazelcastSubscriptionStore(HazelcastInstance hzInstance, CustomConfig customConfig) {
        super(customConfig);

        this.hzInstance = hzInstance;
        this.customConfig = customConfig;

        this.clientSubMultiMap = hzInstance.getMultiMap(BusinessConstants.HAZELCAST_SUB_STORE);
    }

    @Override
    public boolean add(Subscription subscription, boolean onlyMemory) {
        boolean addResult = super.add(subscription);
        if (addResult && !onlyMemory) {
            clientSubMultiMap.put(subscription.getClientId(), subscription);
            return true;
        }

        return addResult;
    }

    @Override
    public boolean remove(Subscription subscription) {
        boolean removeResult = super.remove(subscription);
        if (removeResult) {
            clientSubMultiMap.remove(subscription.getClientId(), subscription);
            return true;
        }

        return false;
    }

    @Override
    public Set<Subscription> findAllBy(String clientId) {
        return Optional.ofNullable(clientSubMultiMap.get(clientId))
                       .map(subCollection -> new HashSet<>(subCollection))
                       .orElse(new HashSet<>());
    }

    @Override
    public void removeAllBy(String clientId) {
        Optional.ofNullable(clientSubMultiMap.get(clientId))
                .ifPresent(subSet -> {
                    subSet.forEach(sub -> {
                        //删除订阅关系
                        super.remove(sub);
                    });

                    clientSubMultiMap.remove(clientId);
                });
    }
}
