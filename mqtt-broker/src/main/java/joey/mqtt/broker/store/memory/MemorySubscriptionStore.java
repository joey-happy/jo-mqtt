package joey.mqtt.broker.store.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.subscription.SubWildcardTree;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.store.ISubscriptionStore;
import joey.mqtt.broker.util.TopicUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内存订阅存储
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class MemorySubscriptionStore implements ISubscriptionStore {
    /**
     * 通配符订阅topic-sub tree
     */
    private SubWildcardTree wildcardSubCache;

    /**
     * 普通订阅topic-sub map
     */
    private ConcurrentHashMap<String, Set<Subscription>> commonSubCache = new ConcurrentHashMap<>();

    /**
     * topic并发操作锁map
     */
    private ConcurrentHashMap<String, AtomicBoolean> lockMap = new ConcurrentHashMap<>();

    public MemorySubscriptionStore(CustomConfig config) {
        wildcardSubCache = new SubWildcardTree();
        wildcardSubCache.init();
    }

    @Override
    public boolean add(Subscription subscription) {
        String topic = subscription.getTopic();

        List<String> topicTokenList = TopicUtils.getTokenList(topic);
        if (CollUtil.isEmpty(topicTokenList)) {
            log.error("MemorySubscriptionStore-addSub topic is not valid.topic={}", topic);
            return false;
        }

        if (SubWildcardTree.isWildcardTopic(topic)) {
            wildcardSubCache.add(topicTokenList, subscription);

        } else {
            //解决添加和删除并发操作出现订阅失败问题
            for(;;) {
                lockMap.putIfAbsent(topic, new AtomicBoolean(false));
                AtomicBoolean atomicBoolean = lockMap.get(topic);

                if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                    Set<Subscription> subSet = commonSubCache.get(topic);

                    if (null == subSet) {
                        commonSubCache.putIfAbsent(topic, new ConcurrentHashSet<>());
                        subSet = commonSubCache.get(topic);
                    }

                    subSet.add(subscription);

                    lockMap.remove(topic);
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public void remove(Subscription subscription) {
        String topic = subscription.getTopic();

        List<String> topicTokenList = TopicUtils.getTokenList(topic);
        if (CollUtil.isEmpty(topicTokenList)) {
            log.error("MemorySubscriptionStore-removeSub topic is not valid.topic={}", topic);
            return;
        }

        if (SubWildcardTree.isWildcardTopic(topic)) {
            wildcardSubCache.remove(topicTokenList, subscription);

        } else {
            Set<Subscription> subSet = commonSubCache.get(topic);

            if (CollectionUtil.isNotEmpty(subSet) && subSet.contains(subscription)) {
                subSet.remove(subscription);

                //如果移除client的订阅关系后 此topic在无人订阅 则删除此topic 释放内存
                if (CollectionUtil.isEmpty(subSet)) {
                    //解决添加和删除并发操作出现订阅失败问题
                    for(;;) {
                        lockMap.putIfAbsent(topic, new AtomicBoolean(false));
                        AtomicBoolean atomicBoolean = lockMap.get(topic);

                        if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                            //获取到锁
                            subSet = commonSubCache.get(topic);
                            if (CollectionUtil.isEmpty(subSet)) {
                                commonSubCache.remove(topic);
                            }

                            //释放锁
                            lockMap.remove(topic);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Subscription> match(String topic) {
        List<Subscription> subscriptionList = new LinkedList<>();

        List<String> topicTokenList = TopicUtils.getTokenList(topic);
        if (CollUtil.isEmpty(topicTokenList)) {
            log.error("MemorySubscriptionStore-match topic is not valid. topic={}", topic);
            return subscriptionList;
        }

        if (StrUtil.isNotBlank(topic)) {
            if (commonSubCache.containsKey(topic)) {
                Set<Subscription> subSet = commonSubCache.get(topic);

                if (CollectionUtil.isNotEmpty(subSet)) {
                    subscriptionList.addAll(subSet);
                }
            }

            List<Subscription> wildcardSubList = wildcardSubCache.getSubListFor(topic, topicTokenList);
            if (CollectionUtil.isNotEmpty(wildcardSubList)) {
                subscriptionList.addAll(wildcardSubList);
            }
        }

        return subscriptionList;
    }

    public String dumpWildcardSubData() {
        return wildcardSubCache.dumpTreeToJson();
    }

    @Override
    public void close() {

    }
}
