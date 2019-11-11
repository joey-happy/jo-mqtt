package joey.mqtt.broker.store;

import joey.mqtt.broker.core.message.CommonPublishMessage;

import java.util.List;

/**
 * retain消息存储
 *
 * @author Joey
 * @date 2019/09/03
 */
public interface IRetainMessageStore extends IStore {
    /**
     * 存储消息
     *
     * @param message
     */
    void add(CommonPublishMessage message);

    /**
     * 删除消息
     *
     * @param topic
     */
    void remove(String topic);

    /**
     * 匹配满足topic的所有消息
     *
     * @param topic
     * @return
     */
    List<CommonPublishMessage> match(String topic);
}
