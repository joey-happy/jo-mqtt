package joey.mqtt.broker.innertraffic;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final String THREAD_NAME_PRE = "RedisInnerTraffic-thread-";

    private static final int CORE_SIZE = 20;

    private static final int MAX_POOL_SIZE = 100;

    private static final long KEEP_ALIVE_TIME = 1L;

    private static final ThreadFactory threadFactory = new ThreadFactory() {
        final AtomicInteger idx = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME_PRE + idx.incrementAndGet());
            return thread;
        }
    };

    private static final RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy() {
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor e) {
            // 继续执行任务
            Thread thread = new Thread(runnable);
            thread.start();
        }
    };

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                                                                CORE_SIZE,
                                                                MAX_POOL_SIZE,
                                                                KEEP_ALIVE_TIME,
                                                                TimeUnit.HOURS,
                                                                new SynchronousQueue<>(),
                                                                threadFactory,
                                                                rejectedExecutionHandler);

    public RedisInnerTraffic(RedisClient redisClient, InnerPublishEventProcessor innerPublishEventProcessor, String nodeName) {
        this.redisClient = redisClient;
        this.innerPublishEventProcessor = innerPublishEventProcessor;
        this.nodeName = nodeName;

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
                                    executor.execute(() -> {
                                        CommonPublishMessage pubMsg = JSONObject.parseObject(message, CommonPublishMessage.class);

                                        //消息来源不是同一个node时候才会继续发布
                                        if (null != pubMsg && ObjectUtil.notEqual(nodeName, pubMsg.getSourceNodeName())) {
                                            innerPublishEventProcessor.publish2Subscribers(pubMsg);
                                        }
                                    });
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
                    }, Constants.REDIS_INNER_TRAFFIC_PUB_CHANNEL);
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

        redisClient.publish(Constants.REDIS_INNER_TRAFFIC_PUB_CHANNEL, jsonMsg);
    }
}
