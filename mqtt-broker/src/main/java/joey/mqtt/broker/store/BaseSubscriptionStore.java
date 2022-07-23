package joey.mqtt.broker.store;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.subscription.SubWildcardTree;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.util.TopicUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基础订阅存储
 *
 * @author Joey
 * @date 2021/03/22
 */
@Slf4j
public abstract class BaseSubscriptionStore implements ISubscriptionStore {
    /**
     * 通配符订阅topic-sub tree
     */
    private SubWildcardTree wildcardTopicSubCache;

    /**
     * 普通订阅topic-sub map
     */
    private ConcurrentHashMap<String, Set<Subscription>> commonTopicSubCache = new ConcurrentHashMap<>();

    /**
     * topic并发操作锁map
     */
    private ConcurrentHashMap<String, AtomicBoolean> topicLockMap = new ConcurrentHashMap<>();

    protected BaseSubscriptionStore(CustomConfig config) {
        wildcardTopicSubCache = new SubWildcardTree();
        wildcardTopicSubCache.init();
    }

    protected boolean add(Subscription subscription) {
        String topic = subscription.getTopic();

        List<String> topicTokenList = TopicUtils.getTopicTokenList(topic);
        if (CollUtil.isEmpty(topicTokenList)) {
            log.error("MemorySubscriptionStore-addSub topic is not valid. clientId={},topic={}", subscription.getClientId(), topic);
            return false;
        }

        if (SubWildcardTree.isWildcardTopic(topic)) {
            wildcardTopicSubCache.add(topicTokenList, subscription);

        } else {
            //解决添加和删除并发操作出现订阅失败问题
            for (;;) {
                topicLockMap.putIfAbsent(topic, new AtomicBoolean(false));
                AtomicBoolean atomicBoolean = topicLockMap.get(topic);

                if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                    Set<Subscription> subSet = commonTopicSubCache.computeIfAbsent(topic, s -> new ConcurrentHashSet<>());
                    subSet.add(subscription);
                    log.debug("MemorySubscriptionStore-addSub success. subscription={}", subscription);

                    topicLockMap.remove(topic);
                    break;
                }
            }
        }

        return true;
    }

    @Override
    public boolean remove(Subscription subscription) {
        String topic = subscription.getTopic();

        List<String> topicTokenList = TopicUtils.getTopicTokenList(topic);
        if (CollUtil.isEmpty(topicTokenList)) {
            log.error("MemorySubscriptionStore-removeSub topic is not valid.topic={}", topic);
            return false;
        }

        if (SubWildcardTree.isWildcardTopic(topic)) {
            wildcardTopicSubCache.remove(topicTokenList, subscription);

        } else {
            Set<Subscription> subSet = commonTopicSubCache.get(topic);

            if (CollectionUtil.isNotEmpty(subSet) && subSet.contains(subscription)) {
                //移除请阅关系
                subSet.remove(subscription);

                //如果移除client的订阅关系后 此topic在无人订阅 则删除此topic 释放内存
                if (CollUtil.isEmpty(subSet)) {
                    //解决添加和删除并发操作出现订阅失败问题
                    for (;;) {
                        topicLockMap.putIfAbsent(topic, new AtomicBoolean(false));
                        AtomicBoolean atomicBoolean = topicLockMap.get(topic);

                        if (null != atomicBoolean && atomicBoolean.compareAndSet(false, true)) {
                            //获取到锁
                            subSet = commonTopicSubCache.get(topic);
                            if (CollUtil.isEmpty(subSet)) {
                                commonTopicSubCache.remove(topic);
                                log.debug("MemorySubscriptionStore-removeSub success. subscription={}", subscription);
                            }

                            //释放锁
                            topicLockMap.remove(topic);
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public List<Subscription> match(String topic) {
        List<Subscription> subscriptionList = new LinkedList<>();

        List<String> topicTokenList = TopicUtils.getTopicTokenList(topic);
        if (CollUtil.isEmpty(topicTokenList)) {
            log.error("MemorySubscriptionStore-match topic is not valid. topic={}", topic);
            return subscriptionList;
        }

        if (StrUtil.isNotBlank(topic)) {
            if (commonTopicSubCache.containsKey(topic)) {
                Set<Subscription> subSet = commonTopicSubCache.get(topic);

                if (CollectionUtil.isNotEmpty(subSet)) {
                    subscriptionList.addAll(subSet);
                }
            }

            List<Subscription> wildcardSubList = wildcardTopicSubCache.getSubListFor(topicTokenList);
            if (CollectionUtil.isNotEmpty(wildcardSubList)) {
                subscriptionList.addAll(wildcardSubList);
            }
        }

        return subscriptionList;
    }

    public String dumpWildcardSubData() {
        return wildcardTopicSubCache.dumpTreeToJson();
    }

    @Override
    public void close() {

    }
}
