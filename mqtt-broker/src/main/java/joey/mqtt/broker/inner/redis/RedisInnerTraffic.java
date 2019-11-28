package joey.mqtt.broker.inner.redis;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.inner.IInnerTraffic;
import joey.mqtt.broker.inner.InnerPublishEventProcessor;
import joey.mqtt.broker.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

/**
 * redis pub sub 实现集群间内部通信
 *
 * @author Joey
 * @date 2019/7/25
 */
@Slf4j
public class RedisInnerTraffic implements IInnerTraffic {
    private final RedisClient redisClient;

    private final InnerPublishEventProcessor innerPublishEventProcessor;

    private final String nodeName;

    public RedisInnerTraffic(RedisClient redisClient, InnerPublishEventProcessor innerPublishEventProcessor, String nodeName) {
        this.redisClient = redisClient;
        this.innerPublishEventProcessor = innerPublishEventProcessor;
        this.nodeName = nodeName;

        subTopic();
    }

    private void subTopic() {
        new Thread(() -> {
            redisClient.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    log.info("RedisInnerTraffic-onMessage. channel={},message={}", channel, message);

                    try {
                        if (StrUtil.isNotBlank(message)) {
                            CommonPublishMessage pubMsg = JSONObject.parseObject(message, CommonPublishMessage.class);

                            //消息来源不是同一个node时候才会继续发布
                            if (null != pubMsg && ObjectUtil.notEqual(nodeName, pubMsg.getSourceNodeName())) {
                                innerPublishEventProcessor.publish2Subscribers(pubMsg);
                            }
                        }
                    } catch (Throwable t) {
                        log.error("RedisInnerTraffic-onMessage error. channel={},message={}", channel, message, t);
                    }
                }

                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    log.info("RedisInnerTraffic-onSubscribe. channel={},subscribedChannels={}", channel, subscribedChannels);
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    log.info("RedisInnerTraffic-onUnsubscribe. channel={},subscribedChannels={}", channel, subscribedChannels);
                }
            }, Constants.REDIS_INNER_TRAFFIC_PUB_CHANNEL);
        }).start();
    }

    /**
     * 发布消息
     * @param message
     */
    @Override
    public void publish(CommonPublishMessage message) {
        redisClient.publish(Constants.REDIS_INNER_TRAFFIC_PUB_CHANNEL, JSON.toJSONString(message));
    }
}
