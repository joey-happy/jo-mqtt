package joey.mqtt.broker.util;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.core.message.CommonPublishMessage;

import java.util.List;

/**
 * 消息工具类
 *
 * @author Joey
 * @date 2019/7/25
 */
public class MessageUtils {
    private MessageUtils() {

    }

    /**
     * 获取较小的qos
     *
     * @param qos1
     * @param qos2
     * @return
     */
    public static int getMinQos(int qos1, int qos2) {
        return qos1 < qos2 ? qos1 : qos2;
    }

    /**
     * 构建connAck消息
     * @param returnCode
     * @return
     */
    public static MqttConnAckMessage buildConnectAckMessage(MqttConnectReturnCode returnCode) {
        return buildConnectAckMessage(returnCode, false);
    }

    /**
     * 构建connAck消息
     * @param returnCode
     * @param sessionPresent
     * @return
     */
    public static MqttConnAckMessage buildConnectAckMessage(MqttConnectReturnCode returnCode, boolean sessionPresent) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, NumConstants.INT_0);
        MqttConnAckVariableHeader variableHeader = new MqttConnAckVariableHeader(returnCode, sessionPresent);
        return new MqttConnAckMessage(fixedHeader, variableHeader);
    }

    /**
     * 构建pub消息
     * @param msg
     * @param qos
     * @param messageId
     * @return
     */
    public static MqttPublishMessage buildPubMsg(CommonPublishMessage msg, MqttQoS qos, int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, qos, msg.isRetain(), NumConstants.INT_0);
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(msg.getTopic(), messageId);
        return new MqttPublishMessage(fixedHeader, variableHeader, Unpooled.buffer().writeBytes(msg.getMessageBody().getBytes()));
    }

    /**
     * 构建subAck消息
     * @param messageId
     * @param qosList
     * @return
     */
    public static MqttMessage buildSubAckMessage(int messageId, List<Integer> qosList) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK,false, MqttQoS.AT_MOST_ONCE,false, NumConstants.INT_0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttSubAckPayload subAckPayload = new MqttSubAckPayload(qosList);
        return new MqttSubAckMessage(fixedHeader, idVariableHeader, subAckPayload);
    }

    /**
     * 构建pubAck消息
     * @param messageId
     * @return
     */
    public static MqttPubAckMessage buildPubAckMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK,false, MqttQoS.AT_MOST_ONCE,false, NumConstants.INT_0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(fixedHeader, idVariableHeader);
    }

    /**
     * 构建pubRec消息
     * @param messageId
     * @return
     */
    public static MqttMessage buildPubRecMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC,false, MqttQoS.AT_MOST_ONCE,false, NumConstants.INT_0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    /**
     * 构建pubComp消息
     * @param messageId
     * @return
     */
    public static MqttMessage buildPubCompMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP,false, MqttQoS.AT_MOST_ONCE,false, NumConstants.INT_0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader, MqttMessageIdVariableHeader.from(messageId));
        return mqttMessage;
    }

    /**
     * 构建pubRel消息
     * @param messageId
     * @param isDup
     * @return
     */
    public static MqttMessage buildPubRelMessage(int messageId, boolean isDup) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, isDup, MqttQoS.AT_MOST_ONCE,false, NumConstants.INT_0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttMessage(fixedHeader, idVariableHeader);
    }

    /**
     * 构建unsubAck消息
     * @param messageId
     * @return
     */
    public static MqttUnsubAckMessage buildUnsubAckMessage(int messageId) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK,false, MqttQoS.AT_MOST_ONCE,false, NumConstants.INT_0);
        MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttUnsubAckMessage(fixedHeader, idVariableHeader);
    }

    /**
     * 构建ping响应消息
     * @return
     */
    public static MqttMessage buildPingRespMessage() {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP,false, MqttQoS.AT_MOST_ONCE,false, NumConstants.INT_0);
        MqttMessage mqttMessage = new MqttMessage(fixedHeader);
        return mqttMessage;
    }

    public static byte[] readBytesAndRewind(ByteBuf payload) {
        byte[] payloadContent = new byte[payload.readableBytes()];
        int mark = payload.readerIndex();
        payload.readBytes(payloadContent);
        payload.readerIndex(mark);
        return payloadContent;
    }

    /**
     * 字节数组拷贝
     * @param bytes
     * @return
     */
    public static byte[] copy(byte[] bytes) {
        byte[] copyBytes = new byte[bytes.length];
        ArrayUtil.copy(bytes, copyBytes, bytes.length);

        return copyBytes;
    }
}
