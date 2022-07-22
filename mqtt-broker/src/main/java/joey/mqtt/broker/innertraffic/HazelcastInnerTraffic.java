package joey.mqtt.broker.innertraffic;

import com.alibaba.fastjson.JSON;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * hazelcast 实现集群间内部通信
 *
 * @author Joey
 * @date 2019/7/25
 */
@Slf4j
public class HazelcastInnerTraffic extends BaseInnerTraffic implements MessageListener<CommonPublishMessage> {
    private final HazelcastInstance hzInstance;

    public HazelcastInnerTraffic(HazelcastInstance hzInstance, InnerPublishEventProcessor innerPublishEventProcessor, CustomConfig customConfig, String nodeName) {
        super(nodeName, innerPublishEventProcessor);

        this.hzInstance = hzInstance;

        //添加集群间topic监听
        ITopic<CommonPublishMessage> topic = hzInstance.getTopic(Constants.HAZELCAST_INNER_TRAFFIC_TOPIC);
        topic.addMessageListener(this);
    }

    /**
     * 发布消息
     * @param message
     */
    @Override
    public void publish(CommonPublishMessage message) {
        log.info("HazelcastInnerTraffic-publish message={}", JSON.toJSONString(message));

        ITopic<CommonPublishMessage> topic = hzInstance.getTopic(Constants.HAZELCAST_INNER_TRAFFIC_TOPIC);
        topic.publish(message);
    }

    /**
     * 监听集群发送的消息
     * @param msg
     */
    @Override
    public void onMessage(Message<CommonPublishMessage> msg) {
        try {
            if (!msg.getPublishingMember().equals(hzInstance.getCluster().getLocalMember())) {
                CommonPublishMessage commonPubMsg = msg.getMessageObject();
                //集群间接收到消息 retain设置为false
                commonPubMsg.setRetain(false);

                log.info("Hazelcast:receive cluster message. nodeName={},message={}", nodeName, commonPubMsg.toString());
                super.publish2Subscribers(commonPubMsg);
            }
        } catch (Exception ex) {
            log.error("Hazelcast:onMessage error. msg={}", msg, ex);
        }
    }
}
