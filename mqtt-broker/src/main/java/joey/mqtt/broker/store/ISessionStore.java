package joey.mqtt.broker.store;

import joey.mqtt.broker.core.client.ClientSession;

/**
 * session存储
 * @author Joey
 * @date 2019/7/22
 */
public interface ISessionStore extends IStore {
    /**
     * 存储会话
     *
     * @param clientSession
     */
    void add(ClientSession clientSession);

    /**
     * 获取会话
     *
     * @param clientId
     * @return
     */
    ClientSession get(String clientId);

    /**
     * 删除会话
     *
     * @param clientId
     */
    void remove(String clientId);

    /**
     * 当前连接session数量
     * @return
     */
    int getSessionCount();
}
