package joey.mqtt.broker.provider;

import io.netty.handler.ssl.SslContext;
import joey.mqtt.broker.auth.AuthUser;
import joey.mqtt.broker.auth.IAuth;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.innertraffic.InnerPublishEventProcessor;
import joey.mqtt.broker.store.*;

import java.util.List;

/**
 * 实现扩展接口定义
 *
 * @author Joey
 * @date 2019/7/23
 */
public interface IExtendProvider {
    /**
     * 初始化sslContext
     *
     * @param enableClientCA
     * @return
     * @throws Exception
     */
    SslContext initSslContext(boolean enableClientCA) throws Exception;

    /**
     * 获取messageId存储实现
     * @return
     */
    IMessageIdStore initMessageIdStore();

    /**
     * 获取session存储实现
     * @return
     */
    ISessionStore initSessionStore();

    /**
     * 获取主题订阅存储实现
     *
     * @param sessionStore
     * @return
     */
    ISubscriptionStore initSubscriptionStore(ISessionStore sessionStore);

    /**
     * 获取retain消息存储实现
     * @return
     */
    IRetainMessageStore initRetainMessageStore();

    /**
     * 获取pubMessage消息存储实现
     * @return
     */
    IDupPubMessageStore initDupPubMessageStore();

    /**
     * 获取pubRelMessage消息存储实现
     * @return
     */
    IDupPubRelMessageStore initDupPubRelMessageStore();

    /**
     * 获取授权管理实现
     * @param userList
     * @return
     */
    IAuth initAuthManager(List<AuthUser> userList);

    /**
     * 获取集群间通信实现
     * @param innerPublishEventProcessor
     * @param nodeName 部署实例唯一标识
     * @return
     */
    IInnerTraffic initInnerTraffic(InnerPublishEventProcessor innerPublishEventProcessor, String nodeName);

    /**
     * 获取事件监听器列表
     * @return
     */
    List<IEventListener> initEventListeners();

    /**
     * 获取节点名称
     * @return
     */
    String getNodeName();
}
