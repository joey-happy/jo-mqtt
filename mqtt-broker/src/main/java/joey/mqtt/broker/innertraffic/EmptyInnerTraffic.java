package joey.mqtt.broker.innertraffic;

import com.alibaba.fastjson.JSON;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 空集群间通信实现
 *
 * @author Joey
 * @date 2019/12/3
 */
@Slf4j
public class EmptyInnerTraffic extends BaseInnerTraffic {

    public EmptyInnerTraffic(String nodeName, InnerPublishEventProcessor innerPublishEventProcessor) {
        super(nodeName, innerPublishEventProcessor);
    }

    @Override
    public void publish(CommonPublishMessage message) {
        log.debug("EmptyInnerTraffic-publish message={}", JSON.toJSONString(message));
    }
}
