package joey.mqtt.broker.store;

import joey.mqtt.broker.core.message.CommonPublishMessage;

import java.util.List;

/**
 * PUBLISH重发消息存储, 当QoS=1和QoS=2时存在该重发机制
 *
 * @author Joey
 * @date 2019/09/03
 */
public interface IDupPubMessageStore extends IStore {
    /**
     * 存储消息
     *
     * @param message
     */
    void add(CommonPublishMessage message);

    /**
     * 获取消息
     *
     * @param clientId
     * @return
     */
    List<CommonPublishMessage> get(String clientId);

    /**
     * 获取消息
     *
     * @param clientId
     * @param messageId
     * @return
     */
    CommonPublishMessage get(String clientId, int messageId);

    /**
     * 删除指定消息
     *
     * @param clientId
     * @param messageId
     */
    void remove(String clientId, int messageId);

    /**
     * 删除用户所有消息
     *
     * @param clientId
     */
    void removeAllFor(String clientId);
}
