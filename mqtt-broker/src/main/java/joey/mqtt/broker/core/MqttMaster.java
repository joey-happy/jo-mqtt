package joey.mqtt.broker.core;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import joey.mqtt.broker.auth.AuthUser;
import joey.mqtt.broker.auth.IAuth;
import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.config.ServerConfig;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.dispatcher.DispatcherCommandCenter;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.processor.*;
import joey.mqtt.broker.exception.MqttException;
import joey.mqtt.broker.innertraffic.IInnerTraffic;
import joey.mqtt.broker.innertraffic.InnerPublishEventProcessor;
import joey.mqtt.broker.provider.IExtendProvider;
import joey.mqtt.broker.store.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * mqtt逻辑处理核心
 *
 * @author Joey
 * @date 2019/7/18
 */
@Slf4j
public class MqttMaster {
    @Getter
    private final Config config;

    private final IMessageIdStore messageIdStore;

    private final ISessionStore sessionStore;

    private final ISubscriptionStore subscriptionStore;

    private final IRetainMessageStore retainMessageStore;

    private final IDupPubMessageStore dupPubMessageStore;

    private final IDupPubRelMessageStore dupPubRelMessageStore;

    private IAuth authManager;

    private IInnerTraffic innerTraffic;

    private final EventListenerExecutor eventListenerExecutor;

    private final List<IEventListener> eventListenerList;

    private final String nodeName;

    private final ConnectEventProcessor connectEventProcessor;

    private final ConnectionLostEventProcessor connectionLostEventProcessor;

    private final SubscribeEventProcessor subscribeEventProcessor;

    private final UnsubscribeEventProcessor unsubscribeEventProcessor;

    private final PublishEventProcessor publishEventProcessor;

    private final PubRecEventProcessor pubRecEventProcessor;

    private final PubCompEventProcessor pubCompEventProcessor;

    private final PubRelEventProcessor pubRelEventProcessor;

    private final DisconnectEventProcessor disconnectEventProcessor;

    private final PubAckEventProcessor pubAckEventProcessor;

    private final PingReqEventProcessor pingReqEventProcessor;

    private final DispatcherCommandCenter dispatcherCommandCenter;

    public MqttMaster(Config config, IExtendProvider extendProvider) {
        this.config = config;
        log.info("Mqtt config info:{}", JSON.toJSONString(config));

        this.messageIdStore = extendProvider.initMessageIdStore();
        this.sessionStore = extendProvider.initSessionStore();
        this.subscriptionStore = extendProvider.initSubscriptionStore(sessionStore);
        this.retainMessageStore = extendProvider.initRetainMessageStore();
        this.dupPubMessageStore = extendProvider.initDupPubMessageStore();
        this.dupPubRelMessageStore = extendProvider.initDupPubRelMessageStore();

        this.eventListenerList = extendProvider.initEventListeners();
        this.eventListenerExecutor = new EventListenerExecutor(eventListenerList);

        this.nodeName = extendProvider.getNodeName();
        if (StrUtil.isBlank(nodeName)) {
            throw new MqttException("Node name is empty.nodeName=" + nodeName);
        }

        ServerConfig serverConfig = config.getServerConfig();

        this.dispatcherCommandCenter = new DispatcherCommandCenter(serverConfig.getDispatcherCount(), serverConfig.getDispatcherQueueSize());

        if (serverConfig.isEnableUserAuth()) {
            List<AuthUser> copyAuthUserList = JSONObject.parseArray(JSON.toJSONString(serverConfig.getAuthUsers()), AuthUser.class);
            this.authManager = extendProvider.initAuthManager(copyAuthUserList);
        }

        this.connectEventProcessor = new ConnectEventProcessor(dispatcherCommandCenter, sessionStore, subscriptionStore, dupPubMessageStore, dupPubRelMessageStore, authManager, serverConfig.isEnableUserAuth(), eventListenerExecutor);

        this.publishEventProcessor = new PublishEventProcessor(dispatcherCommandCenter, sessionStore, subscriptionStore, messageIdStore, retainMessageStore, dupPubMessageStore, eventListenerExecutor, nodeName, authManager);

        InnerPublishEventProcessor innerPublishEventProcessor = new InnerPublishEventProcessor(publishEventProcessor);
        this.innerTraffic = extendProvider.initInnerTraffic(innerPublishEventProcessor, nodeName);

        publishEventProcessor.setInnerTraffic(innerTraffic);

        this.connectionLostEventProcessor = new ConnectionLostEventProcessor(dispatcherCommandCenter, sessionStore, publishEventProcessor,
                                                                             subscriptionStore, dupPubMessageStore, dupPubRelMessageStore,
                                                                             innerTraffic, eventListenerExecutor, nodeName);

        this.pubAckEventProcessor = new PubAckEventProcessor(dispatcherCommandCenter, dupPubMessageStore, eventListenerExecutor);
        this.pubRecEventProcessor = new PubRecEventProcessor(dispatcherCommandCenter, dupPubMessageStore, dupPubRelMessageStore, eventListenerExecutor);
        this.pubRelEventProcessor = new PubRelEventProcessor(dispatcherCommandCenter, eventListenerExecutor);
        this.pubCompEventProcessor = new PubCompEventProcessor(dispatcherCommandCenter, dupPubRelMessageStore, eventListenerExecutor);

        this.subscribeEventProcessor = new SubscribeEventProcessor(dispatcherCommandCenter, sessionStore, subscriptionStore, retainMessageStore, publishEventProcessor, eventListenerExecutor, authManager);
        this.unsubscribeEventProcessor = new UnsubscribeEventProcessor(dispatcherCommandCenter, sessionStore, subscriptionStore, eventListenerExecutor);

        this.pingReqEventProcessor = new PingReqEventProcessor(dispatcherCommandCenter, eventListenerExecutor);

        this.disconnectEventProcessor = new DisconnectEventProcessor(dispatcherCommandCenter, sessionStore, subscriptionStore, dupPubMessageStore, dupPubRelMessageStore, eventListenerExecutor);
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端请求连接服务端
     * @param ctx
     * @param msg
     */
    public void connect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        connectEventProcessor.process(ctx, msg);
    }

    /**
     * 连接丢失
     * 描述：设定时间间隔内没有接收到任何读写请求
     * @param ctx
     */
    public void lostConnection(ChannelHandlerContext ctx) {
        connectionLostEventProcessor.process(ctx, new MqttMessage(null));
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端订阅请求
     * @param ctx
     * @param msg
     */
    public void subscribe(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        subscribeEventProcessor.process(ctx, msg);
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端取消订阅请求
     * @param ctx
     * @param msg
     */
    public void unsubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
        unsubscribeEventProcessor.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：发布消息
     * @param ctx
     * @param msg
     */
    public void publish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        publishEventProcessor.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：发布收到（保证交付第一步）
     * @param ctx
     * @param msg
     */
    public void pubRec(ChannelHandlerContext ctx, MqttMessage msg) {
        pubRecEventProcessor.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：	QoS 2消息发布完成（保证交互第三步）
     * @param ctx
     * @param msg
     */
    public void pubComp(ChannelHandlerContext ctx, MqttMessage msg) {
        pubCompEventProcessor.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：发布释放（保证交付第二步）
     * @param ctx
     * @param msg
     */
    public void pubRel(ChannelHandlerContext ctx, MqttMessage msg) {
        pubRelEventProcessor.process(ctx, msg);
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端断开连接
     * @param ctx
     * @param msg
     */
    public void disconnect(ChannelHandlerContext ctx, MqttMessage msg) {
        disconnectEventProcessor.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：QoS 1消息发布收到确认
     * @param ctx
     * @param msg
     */
    public void pubAck(ChannelHandlerContext ctx, MqttPubAckMessage msg) {
        pubAckEventProcessor.process(ctx, msg);
    }

    /**
     * ping请求
     *
     * @param ctx
     * @param msg
     */
    public void pingReq(ChannelHandlerContext ctx, MqttMessage msg) {
        pingReqEventProcessor.process(ctx, msg);
    }

    /**
     * 资源关闭
     */
    public void close() {
        sessionStore.close();

        subscriptionStore.close();

        retainMessageStore.close();

        dupPubMessageStore.close();

        dupPubRelMessageStore.close();

        dispatcherCommandCenter.close();

        eventListenerExecutor.close();
    }

    /**
     * 获取session数量
     * @return
     */
    public int getSessionCount() {
        return sessionStore.getSessionCount();
    }

    /**
     * 获取client连接信息
     * @param clientId
     */
    public ClientSession getClientInfoFor(String clientId) {
        return sessionStore.get(clientId);
    }

    /**
     * 获取client订阅信息
     * @param clientId
     * @return
     */
    public Set<Subscription> getClientSubInfoFor(String clientId) {
        return subscriptionStore.findAllBy(clientId);
    }
}
