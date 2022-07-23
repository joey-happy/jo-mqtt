package joey.mqtt.broker.store.memory;

import cn.hutool.core.collection.ConcurrentHashSet;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.store.BaseSubscriptionStore;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存订阅存储
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class MemorySubscriptionStore extends BaseSubscriptionStore {
    /**
     * clientId与其相关所有订阅map
     */
    private final ConcurrentHashMap<String, Set<Subscription>> clientSubMap = new ConcurrentHashMap<>();

    public MemorySubscriptionStore(CustomConfig config) {
        super(config);
    }

    @Override
    public boolean add(Subscription subscription, boolean onlyMemory) {
        boolean addResult = super.add(subscription);
        if (addResult && !onlyMemory) {
            String clientId = subscription.getClientId();
            Set<Subscription> subSet = clientSubMap.computeIfAbsent(clientId, s -> new ConcurrentHashSet<>());
            subSet.add(subscription);
            return true;
        }

        return addResult;
    }

    @Override
    public boolean remove(Subscription subscription) {
        boolean removeResult = super.remove(subscription);
        if (removeResult) {
            String clientId = subscription.getClientId();
            Optional.ofNullable(clientSubMap.get(clientId))
                    .ifPresent(subSet -> {
                        subSet.remove(subscription);
                    });

            return true;
        }

        return false;
    }

    @Override
    public Set<Subscription> findAllBy(String clientId) {
        return Optional.ofNullable(clientSubMap.get(clientId))
                       .map(subCollection -> new HashSet<>(subCollection))
                       .orElse(new HashSet<>());
    }

    @Override
    public void removeAllBy(String clientId) {
        Optional.ofNullable(clientSubMap.get(clientId))
                .ifPresent(subSet -> {
                    subSet.forEach(sub -> {
                        //删除订阅关系
                        super.remove(sub);
                    });

                    subSet.clear();
                });
    }
}
