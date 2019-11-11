package joey.mqtt.broker.store;

import joey.mqtt.broker.core.subscription.Subscription;

import java.util.List;

/**
 * 订阅存储
 * @author Joey
 * @date 2019/7/22
 */
public interface ISubscriptionStore extends IStore {
    /**
     * 添加订阅
     */
    boolean add(Subscription subscription);

    /**
     * 删除订阅
     */
    void remove(Subscription subscription);

    /**
     * 匹配满足topic的所有订阅关系
     */
    List<Subscription> match(String topic);
}
