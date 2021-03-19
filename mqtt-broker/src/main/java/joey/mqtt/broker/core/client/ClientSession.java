package joey.mqtt.broker.core.client;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户session对象
 * @author Joey
 * @date 2019/7/22
 */
@Getter
public class ClientSession implements Serializable {
    private final String clientId;

    private final String userName;

    @Getter(AccessLevel.NONE)
    private final Channel channel;

    private final boolean cleanSession;

    @Getter(AccessLevel.NONE)
    private final MqttPublishMessage willMessage;

    private final int keepAliveTimeSeconds;

    private final String createTimeStr;

    public ClientSession(Channel channel, String clientId, String userName, boolean cleanSession, MqttPublishMessage willMessage, int keepAliveTimeSeconds) {
        this.clientId = clientId;
        this.userName = userName;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;

        this.createTimeStr = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
    }

    public boolean isSameChannel(Channel comparedChannel) {
        return this.channel == comparedChannel;
    }

    /**
     * 关闭连接
     */
    public void closeChannel() {
        channel.close();
    }

    /**
     * 发送消息
     * @param msg
     */
    public void sendMsg(Object msg) {
        channel.writeAndFlush(msg);
    }

    /**
     * 获取遗言
     * @return
     */
    public CommonPublishMessage getPubMsgForWillMessage() {
        if (null == willMessage) {
            return null;
        }

        return CommonPublishMessage.convert(willMessage, true, StrUtil.EMPTY);
    }
}
