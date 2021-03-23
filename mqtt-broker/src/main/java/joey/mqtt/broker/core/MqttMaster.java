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

    private final ConnectEventProcessor connectEvent;
    private final ConnectionLostEventProcessor connectionLostEvent;
    private final SubscribeEventProcessor subscribeEvent;
    private final UnsubscribeEventProcessor unsubscribeEvent;
    private final PublishEventProcessor publishEvent;
    private final PubRecEventProcessor pubRecEvent;
    private final PubCompEventProcessor pubCompEvent;
    private final PubRelEventProcessor pubRelEvent;
    private final DisconnectEventProcessor disconnectEvent;
    private final PubAckEventProcessor pubAckEvent;
    private final PingReqEventProcessor pingReqEvent;

    public MqttMaster(Config config, IExtendProvider extendProvider) {
        this.config = config;
        log.info("Mqtt config info:{}", JSON.toJSONString(config));

        messageIdStore = extendProvider.initMessageIdStore();
        sessionStore = extendProvider.initSessionStore();
        subscriptionStore = extendProvider.initSubscriptionStore(sessionStore);
        retainMessageStore = extendProvider.initRetainMessageStore();
        dupPubMessageStore = extendProvider.initDupPubMessageStore();
        dupPubRelMessageStore = extendProvider.initDupPubRelMessageStore();

        eventListenerList = extendProvider.initEventListeners();
        eventListenerExecutor = new EventListenerExecutor(eventListenerList);

        nodeName = extendProvider.getNodeName();
        if (StrUtil.isBlank(nodeName)) {
            throw new MqttException("Node name is empty.nodeName=" + nodeName);
        }

        ServerConfig serverConfig = config.getServerConfig();
        if (serverConfig.isEnableUserAuth()) {
            List<AuthUser> copyAuthUserList = JSONObject.parseArray(JSON.toJSONString(serverConfig.getAuthUsers()), AuthUser.class);
            authManager = extendProvider.initAuthManager(copyAuthUserList);
        }

        connectEvent = new ConnectEventProcessor(sessionStore, subscriptionStore, dupPubMessageStore, dupPubRelMessageStore, authManager, serverConfig.isEnableUserAuth(), eventListenerExecutor);

        publishEvent = new PublishEventProcessor(sessionStore, subscriptionStore, messageIdStore, retainMessageStore, dupPubMessageStore, eventListenerExecutor, nodeName);

        InnerPublishEventProcessor innerPublishEventProcessor = new InnerPublishEventProcessor(publishEvent);
        innerTraffic = extendProvider.initInnerTraffic(innerPublishEventProcessor, nodeName);

        publishEvent.setInnerTraffic(innerTraffic);

        connectionLostEvent = new ConnectionLostEventProcessor(sessionStore, publishEvent, innerTraffic, eventListenerExecutor, nodeName);

        pubAckEvent = new PubAckEventProcessor(dupPubMessageStore, eventListenerExecutor);
        pubRecEvent = new PubRecEventProcessor(dupPubMessageStore, dupPubRelMessageStore, eventListenerExecutor);
        pubRelEvent = new PubRelEventProcessor(eventListenerExecutor);
        pubCompEvent = new PubCompEventProcessor(dupPubRelMessageStore, eventListenerExecutor);

        subscribeEvent = new SubscribeEventProcessor(sessionStore, subscriptionStore, retainMessageStore, publishEvent, eventListenerExecutor);
        unsubscribeEvent = new UnsubscribeEventProcessor(sessionStore, subscriptionStore, eventListenerExecutor);

        pingReqEvent = new PingReqEventProcessor(eventListenerExecutor);

        disconnectEvent = new DisconnectEventProcessor(sessionStore, subscriptionStore, dupPubMessageStore, dupPubRelMessageStore, eventListenerExecutor);
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端请求连接服务端
     * @param ctx
     * @param msg
     */
    public void connect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        connectEvent.process(ctx, msg);
    }

    /**
     * 连接丢失
     * 描述：设定时间间隔内没有接收到任何读写请求
     * @param ctx
     */
    public void lostConnection(ChannelHandlerContext ctx) {
        connectionLostEvent.process(ctx, new MqttMessage(null));
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端订阅请求
     * @param ctx
     * @param msg
     */
    public void subscribe(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        subscribeEvent.process(ctx, msg);
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端取消订阅请求
     * @param ctx
     * @param msg
     */
    public void unsubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
        unsubscribeEvent.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：发布消息
     * @param ctx
     * @param msg
     */
    public void publish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        publishEvent.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：发布收到（保证交付第一步）
     * @param ctx
     * @param msg
     */
    public void pubRec(ChannelHandlerContext ctx, MqttMessage msg) {
        pubRecEvent.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：	QoS 2消息发布完成（保证交互第三步）
     * @param ctx
     * @param msg
     */
    public void pubComp(ChannelHandlerContext ctx, MqttMessage msg) {
        pubCompEvent.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：发布释放（保证交付第二步）
     * @param ctx
     * @param msg
     */
    public void pubRel(ChannelHandlerContext ctx, MqttMessage msg) {
        pubRelEvent.process(ctx, msg);
    }

    /**
     * 报文流动方向：客户端到服务端
     * 描述：客户端断开连接
     * @param ctx
     * @param msg
     */
    public void disconnect(ChannelHandlerContext ctx, MqttMessage msg) {
        disconnectEvent.process(ctx, msg);
    }

    /**
     * 报文流动方向：两个方向都允许
     * 描述：QoS 1消息发布收到确认
     * @param ctx
     * @param msg
     */
    public void pubAck(ChannelHandlerContext ctx, MqttPubAckMessage msg) {
        pubAckEvent.process(ctx, msg);
    }

    /**
     * ping请求
     *
     * @param ctx
     * @param msg
     */
    public void pingReq(ChannelHandlerContext ctx, MqttMessage msg) {
        pingReqEvent.process(ctx, msg);
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
