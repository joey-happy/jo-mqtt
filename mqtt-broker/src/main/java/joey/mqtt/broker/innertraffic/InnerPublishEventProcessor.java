package joey.mqtt.broker.innertraffic;

import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.event.processor.PublishEventProcessor;

/**
 * 内部通信pub事件处理
 *
 * @author Joey
 * @date 2019/9/7
 */
public class InnerPublishEventProcessor {
    private final PublishEventProcessor publishEventProcessor;

    public InnerPublishEventProcessor(PublishEventProcessor publishEventProcessor) {
        this.publishEventProcessor = publishEventProcessor;
    }

    /**
     * 发布消息到所有订阅者
     * @param pubMsg
     */
    public void publish2Subscribers(CommonPublishMessage pubMsg) {
        publishEventProcessor.publish2Subscribers(pubMsg);
    }

}
