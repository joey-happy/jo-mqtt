package joey.mqtt.broker.inner;

import joey.mqtt.broker.core.message.CommonPublishMessage;

/**
 * 集群间通信
 *
 * @author Joey
 * @date 2019/7/25
 */
public interface IInnerTraffic {
    /**
     * 发布消息
     * @param message
     */
    void publish(CommonPublishMessage message);
}
