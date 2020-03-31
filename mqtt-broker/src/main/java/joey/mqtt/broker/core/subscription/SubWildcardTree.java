package joey.mqtt.broker.core.subscription;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static joey.mqtt.broker.Constants.*;

/**
 * 订阅通配符tree
 *
 * 通配符规则：https://www.jianshu.com/p/fd8b379225fe
 *
 * @author Joey
 * @date 2019/8/28
 */
@Slf4j
public class SubWildcardTree {
    private SubNodeWrapper root;

    public void init() {
        root = new SubNodeWrapper(new SubNode(TOKEN_ROOT, TOKEN_ROOT));
    }

    /**
     * 添加订阅
     *
     * @param topicTokenList
     * @param subscription
     */
    public void add(List<String> topicTokenList, Subscription subscription) {
        Action res;

        do {
            res = insert(topicTokenList, subscription);
        } while (res == Action.REPEAT);
    }

    private Action insert(List<String> topicTokenList, Subscription subscription) {
        //1.找到匹配的节点
        SubNodeWrapper matchNodeWrapper = this.root;
        int tokenMatchIndex = -1;
        for (int i = 0; i < topicTokenList.size(); i++) {
            SubNodeWrapper subNodeWrapper = matchNodeWrapper.mainNode().matchChild(topicTokenList.get(i));
            if (null == subNodeWrapper) {
                break;
            }

            matchNodeWrapper = subNodeWrapper;
            tokenMatchIndex = i;
        }

        //2.如果节点完全匹配topic 即:所订阅的topic在当前树中存在 则在当前节点添加订阅信息
        if (tokenMatchIndex == (topicTokenList.size() - 1)) {
            SubNode oldSubNode = matchNodeWrapper.mainNode();
            SubNode updateSubNode = oldSubNode.copy();
            updateSubNode.subscriptions.add(subscription);

            return matchNodeWrapper.compareAndSet(oldSubNode, updateSubNode) ? Action.OK : Action.REPEAT;
        }

        //3.如果没有完全匹配topic 即：需要在当前节点下添加新节点
        //3.1获取到剩余没有匹配的token列表，构建subNodeWrapper节点并追加到当前匹配节点
        List<String> remainTokenList = topicTokenList.subList(tokenMatchIndex + 1, topicTokenList.size());
        SubNodeWrapper newSubNodeWrapper = buildNewSubNodeWrapperTree(matchNodeWrapper.mainNode().fullPath, remainTokenList, subscription);

        //4.替换新节点
        SubNode oldSubNode = matchNodeWrapper.mainNode();
        SubNode updateSubNode = oldSubNode.copy();
        updateSubNode.children.add(newSubNodeWrapper);

        return matchNodeWrapper.compareAndSet(oldSubNode, updateSubNode) ? Action.OK : Action.REPEAT;
    }

    /**
     * 构建一颗带有路径的树
     * @param parentPath
     * @param tokenList
     * @return
     */
    private SubNodeWrapper buildNewSubNodeWrapperTree(String parentPath, List<String> tokenList, Subscription subscription) {
        SubNodeWrapper headNodeWrapper = null;
        SubNodeWrapper currentNodeWrapper = null;

        for (String remainToken : tokenList) {
            if (null == currentNodeWrapper) {
                String fullPath = String.join(StrUtil.SLASH, parentPath, remainToken);
                SubNodeWrapper newNodeWrapper = new SubNodeWrapper(new SubNode(remainToken, fullPath));
                headNodeWrapper = newNodeWrapper;
                currentNodeWrapper = headNodeWrapper;

            } else {
                String pPath = currentNodeWrapper.mainNode().fullPath;
                String fullPath = String.join(StrUtil.SLASH, pPath, remainToken);
                SubNodeWrapper newNodeWrapper = new SubNodeWrapper(new SubNode(remainToken, fullPath));
                currentNodeWrapper.mainNode().children.add(newNodeWrapper);

                currentNodeWrapper = newNodeWrapper;
            }
        }

        //尾部节点添加订阅关系
        currentNodeWrapper.mainNode().subscriptions.add(subscription);

        return headNodeWrapper;
    }

    private Action delete(List<String> topicTokenList, Subscription subscription) {
        //1.找到匹配的节点
        SubNodeWrapper matchNodeWrapper = this.root;
        for (String token : topicTokenList) {
            SubNodeWrapper subNodeWrapper = matchNodeWrapper.mainNode().matchChild(token);
            if (null == subNodeWrapper) {
                break;
            }

            matchNodeWrapper = subNodeWrapper;
        }

        //2.没有找到匹配节点
        if (matchNodeWrapper == this.root) {
            return Action.OK;
        }

        //3.找到匹配节点 删除订阅
        SubNode oldSubNode = matchNodeWrapper.mainNode();
        SubNode updateSubNode = oldSubNode.copy();
        updateSubNode.subscriptions.remove(subscription);

        //TODO 只是简单删除订阅关系 节点仍然保留 没有考虑 节点订阅为空 删除节点等操作 (要处理 需要考虑并发问题)
        return matchNodeWrapper.compareAndSet(oldSubNode, updateSubNode) ? Action.OK : Action.REPEAT;
    }

    /**
     * 删除订阅
     * @param topicTokenList
     * @param subscription
     */
    public void remove(List<String> topicTokenList, Subscription subscription) {
        Action res;

        do {
            res = delete(topicTokenList, subscription);
        } while (res == Action.REPEAT);
    }

    public List<Subscription> getSubListFor(String topic, List<String> topicTokenList) {
        return new ArrayList<>(match(topic, topicTokenList));
    }

    /**
     * 查找符合topic的所有订阅
     * @param topic
     * @param topicTokenList
     * @return
     */
    private Set<Subscription> match(String topic, List<String> topicTokenList) {
        Set<Subscription> subSet = recursiveMatch(topicTokenList, this.root);

        return subSet;
    }

    /**
     * 递归查找匹配节点的订阅集合
     * @param topicTokenList
     * @param subNodeWrapper
     * @return
     */
    private Set<Subscription> recursiveMatch(List<String> topicTokenList, SubNodeWrapper subNodeWrapper) {
        SubNode currentNode = subNodeWrapper.mainNode();

        //匹配'#' 则返回当前节点的所有订阅者
        if (TOKEN_MULTI.equals(currentNode.token)) {
            return currentNode.subscriptions;
        }

        int tokenSize = topicTokenList.size();
        String currentToken = StrUtil.EMPTY;
        String nextToken = StrUtil.EMPTY;
        if (tokenSize > 0) {
            currentToken = topicTokenList.get(0);
        }

        if (tokenSize > 1) {
            nextToken = topicTokenList.get(1);
        }

        //匹配'+' 或者 匹配当前节点token 或者 匹配root节点 则继续遍历子节点
        if (TOKEN_ROOT.equals(currentNode.token) || TOKEN_SINGLE.equals(currentNode.token) || currentNode.token.equals(currentToken)) {
            int subIndex = 0;

            //如果当前节点是根节点 则下一个token为当前token
            if (TOKEN_ROOT.equals(currentNode.token)) {
                nextToken = currentToken;
                subIndex = -1;
            }

            Set<Subscription> subscriptions = new HashSet<>();
            //如果下一个token为空 则证明当前token是最后一个token
            if (StrUtil.EMPTY.equals(nextToken)) {
                subscriptions.addAll(currentNode.subscriptions);

            } else {
                for (SubNodeWrapper childSubNode : currentNode.children) {
                    List<String> remainTopicTokenList = topicTokenList.subList(subIndex + 1, topicTokenList.size());
                    subscriptions.addAll(recursiveMatch(remainTopicTokenList, childSubNode));
                }
            }

            return subscriptions;
        }

        return Collections.emptySet();
    }

    /**
     *
     * @return
     */
    public String dumpTreeToJson() {
        return JSON.toJSONString(this.root);
    }

    /**
     * 含有通配符topic
     *
     * @param topic
     * @return
     */
    public static boolean isWildcardTopic(String topic) {
        return StrUtil.containsAny(topic, TOKEN_MULTI) || StrUtil.containsAny(topic, TOKEN_SINGLE);
    }

    //=================================================================
    private enum Action {
        OK, REPEAT
    }

    /**
     * 订阅节点包装 目的:并发添加和删除锁控制
     */
    @Getter
    private static class SubNodeWrapper {
        private AtomicReference<SubNode> mainNode = new AtomicReference<>();

        SubNodeWrapper(SubNode mainNode) {
            this.mainNode.set(mainNode);
        }

        boolean compareAndSet(SubNode old, SubNode newNode) {
            return mainNode.compareAndSet(old, newNode);
        }

        SubNode mainNode() {
            return this.mainNode.get();
        }
    }

    /**
     * 订阅节点
     */
    @Getter
    private class SubNode {
        @JSONField(ordinal = 1)
        private String fullPath;

        @JSONField(ordinal = 2)
        private String token;

        @JSONField(ordinal = 3)
        private Set<Subscription> subscriptions = new ConcurrentHashSet<>();

        @JSONField(ordinal = 4)
        private Set<SubNodeWrapper> children = new ConcurrentHashSet<>();

        SubNode(String token, String fullPath) {
            this.token = token;
            this.fullPath = fullPath;
        }

        SubNode(String token, String fullPath, Set<Subscription> subscriptions, Set<SubNodeWrapper> children) {
            this.token = token;
            this.fullPath = fullPath;
            this.subscriptions = subscriptions;
            this.children = children;
        }

        /**
         * 对象拷贝
         */
        SubNode copy() {
            return new SubNode(this.token, this.fullPath, this.subscriptions, this.children);
        }

        /**
         * 根据token找到匹配节点
         * @param token
         * @return
         */
        SubNodeWrapper matchChild(String token) {
            for (SubNodeWrapper child : children) {
                if (child.mainNode().token.equals(token)) {
                    return child;
                }
            }

            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SubNode subNode = (SubNode) o;
            return Objects.equals(fullPath, subNode.fullPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fullPath);
        }
    }
}
