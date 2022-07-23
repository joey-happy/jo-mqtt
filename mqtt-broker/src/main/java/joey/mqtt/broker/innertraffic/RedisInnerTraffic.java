package joey.mqtt.broker.innertraffic;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.core.message.CommonPublishMessage;
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
public class RedisInnerTraffic extends BaseInnerTraffic {
    private final RedisClient redisClient;

    public RedisInnerTraffic(RedisClient redisClient, InnerPublishEventProcessor innerPublishEventProcessor, String nodeName) {
        super(nodeName, innerPublishEventProcessor);
        this.redisClient = redisClient;

        subTopic();
    }

    private void subTopic() {
        new Thread(() -> {
            //防止redis连接意外断开不能重新订阅
            for (;;) {
                try {
                    redisClient.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            try {
                                if (StrUtil.isNotBlank(message)) {
                                    log.info("RedisInnerTraffic-onMessage. nodeName={},channel={},message={}", nodeName, channel, message);
                                    CommonPublishMessage pubMsg = JSONObject.parseObject(message, CommonPublishMessage.class);

                                    //消息来源不是同一个node时候才会继续发布
                                    if (ObjectUtil.isNotNull(pubMsg) && ObjectUtil.notEqual(nodeName, pubMsg.getSourceNodeName())) {
                                        publish2Subscribers(pubMsg);
                                    }
                                }
                            } catch (Throwable t) {
                                log.error("RedisInnerTraffic-onMessage error. nodeName={},channel={},message={}", nodeName, channel, message, t);
                            }
                        }

                        @Override
                        public void onSubscribe(String channel, int subscribedChannels) {
                            log.info("RedisInnerTraffic-onSubscribe. nodeName={},channel={},subscribedChannels={}", nodeName, channel, subscribedChannels);
                        }

                        @Override
                        public void onUnsubscribe(String channel, int subscribedChannels) {
                            log.info("RedisInnerTraffic-onUnsubscribe. nodeName={},channel={},subscribedChannels={}", nodeName, channel, subscribedChannels);
                        }
                    }, BusinessConstants.REDIS_INNER_TRAFFIC_PUB_CHANNEL);
                } catch (Exception ex) {
                    log.error("RedisInnerTraffic-subTopic error. nodeName={}", nodeName, ex);
                }
            }
        }).start();
    }

    /**
     * 发布消息
     * @param message
     */
    @Override
    public void publish(CommonPublishMessage message) {
        String jsonMsg = JSON.toJSONString(message);
        log.debug("RedisInnerTraffic-publish message={}", jsonMsg);

        redisClient.publish(BusinessConstants.REDIS_INNER_TRAFFIC_PUB_CHANNEL, jsonMsg);
    }
}
