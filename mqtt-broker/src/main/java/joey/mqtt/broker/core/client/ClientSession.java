package joey.mqtt.broker.core.client;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.core.subscription.Subscription;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 用户session对象
 * @author Joey
 * @date 2019/7/22
 */
@Getter
public class ClientSession implements Serializable {
    private final String clientId;

    private final String userName;

    private volatile Channel channel;

    private final boolean cleanSession;

    @Getter(AccessLevel.NONE)
    private volatile MqttPublishMessage willMessage;

    private final String createTimeStr;

    private final Set<Subscription> subSet = new ConcurrentHashSet<>();

    public ClientSession(Channel channel, String clientId, String userName, boolean cleanSession, MqttPublishMessage willMessage) {
        this.clientId = clientId;
        this.userName = userName;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;

        this.createTimeStr = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
    }

    public void addSub(Subscription sub) {
        subSet.add(sub);
    }

    public void removeSub(Subscription sub) {
        subSet.remove(sub);
    }

    public void removeAllSub() {
        subSet.clear();
    }

    public Set<Subscription> findAllSubInfo() {
        return subSet;
    }

    /**
     * 关闭连接
     */
    public synchronized void closeChannel() {
        if (null != channel) {
            channel.close();
        }
    }

    /**
     * 发送消息
     * @param msg
     */
    public synchronized void sendMsg(Object msg) {
        if (null != channel) {
            channel.writeAndFlush(msg);
        }
    }

    /**
     * 获取遗言
     * @return
     */
    public synchronized CommonPublishMessage getPubMsgForWillMessage() {
        if (null == willMessage) {
            return null;
        }

        return CommonPublishMessage.convert(willMessage, true, StrUtil.EMPTY);
    }

    /**
     * 重置channel和遗言
     */
    public synchronized void resetChannelAndWillMsg() {
        this.channel = null;
        this.willMessage = null;
    }
}
