package joey.mqtt.broker.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import joey.mqtt.broker.exception.MqttException;

import java.net.InetSocketAddress;

/**
 * netty工具类
 *
 * @author Joey
 * @date 2019-09-01
 */
public final class NettyUtils {
    private static final String CLIENT_ID = "clientId";

    private static final AttributeKey<String> ATTR_CLIENT_ID = AttributeKey.valueOf(CLIENT_ID);

    private static final String USER_NAME = "userName";

    public static final AttributeKey<String> ATTR_USER_NAME = AttributeKey.valueOf(USER_NAME);

    private NettyUtils() {

    }

    /**
     * 设置clientId属性
     *
     * @param channel
     * @param clientId
     * @param userName
     */
    public static void clientInfo(Channel channel, String clientId, String userName) {
        clientId(channel, clientId);

        userName(channel, userName);
    }

    /**
     * 设置clientId属性
     *
     * @param channel
     * @param clientId
     */
    public static void clientId(Channel channel, String clientId) {
        channel.attr(NettyUtils.ATTR_CLIENT_ID).set(clientId);
    }

    /**
     * 获取clientId属性
     *
     * @param channel
     * @return
     */
    public static String clientId(Channel channel) {
        return channel.attr(NettyUtils.ATTR_CLIENT_ID).get();
    }

    /**
     * 设置userName属性
     *
     * @param channel
     * @param userName
     */
    public static void userName(Channel channel, String userName) {
        channel.attr(ATTR_USER_NAME).set(userName);
    }

    /**
     * 获取userName属性
     *
     * @param channel
     * @return
     */
    public static String userName(Channel channel) {
        return channel.attr(ATTR_USER_NAME).get();
    }

    /**
     * 获取远程连接ip
     *
     * @param channel
     * @return
     */
    public static String getRemoteIp(Channel channel) {
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
            return socketAddress.getAddress().getHostAddress();
        } catch (Throwable t) {
            //ignore
        }

        return StrUtil.EMPTY;
    }

    /**
     * 检查消息合法性
     *
     * @param msg
     * @return
     */
    public static void checkMessage(MqttMessage msg) {
        DecoderResult decoderResult = msg.decoderResult();
        if (ObjectUtil.isNotNull(decoderResult) && decoderResult.isFailure()) {
            throw new MqttException("Invalid massage", decoderResult.cause());
        }

        if (ObjectUtil.isNull(msg.fixedHeader())) {
            throw new MqttException("Unknown packet, no fixedHeader present, no cause provided");
        }
    }
}
