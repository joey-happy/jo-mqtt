package joey.mqtt.broker.event.processor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.auth.IAuth;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.event.listener.EventListenerExecutor;
import joey.mqtt.broker.event.listener.IEventListener;
import joey.mqtt.broker.event.message.ConnectEventMessage;
import joey.mqtt.broker.store.IDupPubMessageStore;
import joey.mqtt.broker.store.IDupPubRelMessageStore;
import joey.mqtt.broker.store.ISessionStore;
import joey.mqtt.broker.store.ISubscriptionStore;
import joey.mqtt.broker.util.MessageUtils;
import joey.mqtt.broker.util.NettyUtils;
import joey.mqtt.broker.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.*;

/**
 * 连接事件处理
 *
 * @author Joey
 * @date 2019/7/22
 */
@Slf4j
public class ConnectEventProcessor implements IEventProcessor<MqttConnectMessage> {
    private final ISessionStore sessionStore;

    private final IAuth authManager;

    private final ISubscriptionStore subStore;

    private final IDupPubMessageStore dupPubMessageStore;

    private final IDupPubRelMessageStore dupPubRelMessageStore;

    private final boolean useAuth;

    private final EventListenerExecutor eventListenerExecutor;

    public ConnectEventProcessor(ISessionStore sessionStore, ISubscriptionStore subStore, IDupPubMessageStore dupPubMessageStore, IDupPubRelMessageStore dupPubRelMessageStore, IAuth authManager, boolean useAuth, EventListenerExecutor eventListenerExecutor) {
        this.sessionStore = sessionStore;
        this.subStore = subStore;
        this.dupPubMessageStore = dupPubMessageStore;
        this.dupPubRelMessageStore = dupPubRelMessageStore;
        this.authManager = authManager;
        this.useAuth = useAuth;
        this.eventListenerExecutor = eventListenerExecutor;
    }

    @Override
    public void process(ChannelHandlerContext ctx, MqttConnectMessage message) {
        Channel channel = ctx.channel();
        MqttConnectPayload payload = message.payload();

        //检查clientId 必填项
        String clientId = payload.clientIdentifier();
        Stopwatch stopwatch = Stopwatch.start();
        log.info("Process-connect:start. clientId={},userName={}", clientId, payload.userName());

        if (!validClientId(clientId)) {
            channel.writeAndFlush(MessageUtils.buildConnectAckMessage(CONNECTION_REFUSED_IDENTIFIER_REJECTED));
            channel.close();
            log.error("Process-connect:error. ClientId is empty.");
            return;
        }

        //校验版本信息
        MqttConnectVariableHeader variableHeader = message.variableHeader();
        if (!validVersion(variableHeader.version())) {
            channel.writeAndFlush(MessageUtils.buildConnectAckMessage(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION));
            channel.close();
            log.error("Process-connect:error. Mqtt connect version not supported. clientId={},userName={},version={}", clientId, payload.userName(), variableHeader.version());
            return;
        }

        //校验授权
        String userName = StrUtil.EMPTY;
        if (useAuth) {
            userName = payload.userName();
            byte[] passwordInBytes = payload.passwordInBytes();

            if (!authManager.checkValid(userName, passwordInBytes)) {
                channel.writeAndFlush(MessageUtils.buildConnectAckMessage(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD));
                channel.close();
                log.error("Process-connect:error. Unauthorized user. clientId={},userName={}", clientId, payload.userName());
                return;
            }
        }

        //处理旧连接
        handleOldSession(clientId);

        //重置keepAlive超时时间
        resetKeepAliveTimeout(channel, message);

        //设置遗言信息
        MqttPublishMessage willMessage = null;
        if (variableHeader.isWillFlag()) {
            willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(variableHeader.willQos()), variableHeader.isWillRetain(), 0),
                    new MqttPublishVariableHeader(payload.willTopic(), 0), Unpooled.buffer().writeBytes(payload.willMessageInBytes()));
            log.info("Process-connect:store will message. clientId={},userName={}", clientId, payload.userName());
        }

        //构建新session信息
        boolean cleanSession = variableHeader.isCleanSession();
        ClientSession newClientSession = new ClientSession(channel, clientId, userName, cleanSession, willMessage);

        //存储session信息
        sessionStore.add(newClientSession);
        log.info("Process-connect:store new session. clientId={},userName={}", clientId, payload.userName());

        //设置channel通用属性
        NettyUtils.clientInfo(channel, clientId, userName);

        //发送ack回执
        MqttConnAckMessage connectResp = MessageUtils.buildConnectAckMessage(MqttConnectReturnCode.CONNECTION_ACCEPTED, !cleanSession);
        channel.writeAndFlush(connectResp);
        log.info("Process-connect:ack successfully. clientId={},userName={}", clientId, payload.userName());

        //如果cleanSession为0,需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
        if (!cleanSession) {
            sendDupMessage(channel, clientId);
        }

        log.info("Process-connect:end. clientId={},userName={},timeCost={}ms", clientId, payload.userName(), stopwatch.elapsedMills());

        //连接事件监听处理
        eventListenerExecutor.execute(new ConnectEventMessage(message), IEventListener.Type.CONNECT);
    }

    /**
     * 处理旧连接
     * @param clientId
     */
    private void handleOldSession(String clientId) {
        ClientSession oldClientSession = sessionStore.get(clientId);
        //存在旧连接
        if (null != oldClientSession) {
            Boolean cleanSession = oldClientSession.isCleanSession();

            if (cleanSession) {
                Set<Subscription> subSet = oldClientSession.getAllSubInfo();
                if (CollectionUtil.isNotEmpty(subSet)) {
                    Iterator<Subscription> iterator = subSet.iterator();

                    //删除订阅关系
                    while (iterator.hasNext())  {
                        subStore.remove(iterator.next());
                    }
                }

                sessionStore.remove(clientId);
                dupPubMessageStore.removeAllFor(clientId);
                dupPubRelMessageStore.removeAllFor(clientId);
            }

            //关闭旧连接
            Channel oldChannel = oldClientSession.getChannel();
            oldChannel.close();
            log.info("Process-connect close old channel. clientId={}", clientId);
        }
    }

    /**
     * 如果cleanSession为0,需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
     *
     * @param channel
     * @param clientId
     */
    private void sendDupMessage(Channel channel, String clientId) {
        //Qos1:重新发送pub之后没有收到ack的消息
        //Qos2:重新发送pub之后没有收到pubRel的消息
        List<CommonPublishMessage> pubMsgList = dupPubMessageStore.get(clientId);
        if (CollUtil.isNotEmpty(pubMsgList)) {
            pubMsgList.forEach(msg -> {
                MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(msg.getMqttQoS()), false, 0);
                MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(msg.getTopic(), msg.getMessageId());

                channel.writeAndFlush(new MqttPublishMessage(fixedHeader, variableHeader, Unpooled.buffer().writeBytes(msg.getMessageBody().getBytes())));
                log.info("Process-connect:send dup message. clientId={},topic={},createTime={}", clientId, msg.getTopic(), DateUtil.format(new Date(msg.getCreateTimestamp()), DatePattern.PURE_DATETIME_PATTERN));
            });
        }

        //Qos2
        //重新发送pubRel之后没有收到pubComp的消息
        List<CommonPublishMessage> pubRelMsgList = dupPubRelMessageStore.get(clientId);
        if (CollUtil.isNotEmpty(pubRelMsgList)) {
            pubRelMsgList.forEach(msg -> {
                channel.writeAndFlush(MessageUtils.buildPubRelMessage(msg.getMessageId(), true));
                log.info("Process-connect:send dup publish release message. clientId={},messageId={},createTime={}", clientId, msg.getMessageId(), DateUtil.format(new Date(msg.getCreateTimestamp()), DatePattern.PURE_DATETIME_PATTERN));
            });
        }
    }

    /**
     * 重置keepAlive时间
     * @param channel
     * @param msg
     */
    private void resetKeepAliveTimeout(Channel channel, MqttConnectMessage msg) {
        int keepAliveTimeSeconds = msg.variableHeader().keepAliveTimeSeconds();

        if (keepAliveTimeSeconds > 0) {
            if (channel.pipeline().names().contains(Constants.HANDLER_IDLE_STATE)) {
                channel.pipeline().remove(Constants.HANDLER_IDLE_STATE);
            }

            int finalKeepAliveTimeSeconds = Math.round(keepAliveTimeSeconds * 1.5f);
            channel.pipeline().addFirst(Constants.HANDLER_IDLE_STATE, new IdleStateHandler(0, 0, finalKeepAliveTimeSeconds));
        }
    }

    private boolean validClientId(String clientId) {
        return StrUtil.isNotBlank(clientId);
    }

    private boolean validVersion(int version) {
        boolean valid = false;

        for (MqttVersion mqttVersion : MqttVersion.values()) {
            if (mqttVersion.protocolLevel() == version) {
                valid = true;
                break;
            }
        }

        return valid;
    }
}
