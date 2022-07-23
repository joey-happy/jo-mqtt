package joey.mqtt.broker.store;

/**
 * 消息id存储
 *
 * @author Joey
 * @date 2019/9/3
 */
public interface IMessageIdStore {
    /**
     * 获取messageId
     *
     * @param clientId
     * @return
     */
    int getNextMessageId(String clientId);
}
