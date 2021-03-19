package joey.mqtt.broker.store;

import joey.mqtt.broker.core.subscription.Subscription;

import java.util.List;
import java.util.Set;

/**
 * 订阅存储
 * @author Joey
 * @date 2019/7/22
 */
public interface ISubscriptionStore extends IStore {
    /**
     * 添加订阅
     */
    boolean add(Subscription subscription, boolean onlyMemory);

    /**
     * 删除订阅
     */
    boolean remove(Subscription subscription);

    /**
     * 匹配满足topic的所有订阅关系
     */
    List<Subscription> match(String topic);

    /**
     * 根据clientId查找所有订阅
     * @param clientId
     * @return
     */
    Set<Subscription> findAllBy(String clientId);

    /**
     * 根据clientId删除所有订阅
     * @param clientId
     */
    void removeAllBy(String clientId);
}
