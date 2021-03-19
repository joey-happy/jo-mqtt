package joey.mqtt.broker.store.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.store.BaseSubscriptionStore;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
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
        if(addResult && !onlyMemory) {
            String clientId = subscription.getClientId();
            Set<Subscription> subSet = clientSubMap.get(clientId);

            if (null == subSet) {
                clientSubMap.putIfAbsent(clientId, new ConcurrentHashSet<>());
                subSet = clientSubMap.get(clientId);
            }

            subSet.add(subscription);
            return true;
        }

        return addResult;
    }

    @Override
    public boolean remove(Subscription subscription) {
        boolean removeResult = super.remove(subscription);
        if(removeResult) {
            String clientId = subscription.getClientId();
            Set<Subscription> subSet = clientSubMap.get(clientId);

            if (CollUtil.isNotEmpty(subSet) && subSet.contains(subscription)) {
                subSet.remove(subscription);
            }

            return true;
        }

        return false;
    }

    @Override
    public Set<Subscription> findAllBy(String clientId) {
        return CollUtil.emptyIfNull(clientSubMap.get(clientId));
    }

    @Override
    public void removeAllBy(String clientId) {
        Set<Subscription> subSet = clientSubMap.get(clientId);

        if (CollUtil.isNotEmpty(subSet)) {
            Iterator<Subscription> iterator = subSet.iterator();

            //删除订阅关系
            while (iterator.hasNext()) {
                super.remove(iterator.next());
            }

            subSet.clear();
        }
    }
}
