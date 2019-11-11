package joey.mqtt.broker.core.client;

import cn.hutool.core.collection.ConcurrentHashSet;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import joey.mqtt.broker.core.subscription.Subscription;
import lombok.Getter;

import java.io.Serializable;
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

    private final Channel channel;

    private final boolean cleanSession;

    private final MqttPublishMessage willMessage;

    private Set<Subscription> subSet = new ConcurrentHashSet<>();

    public ClientSession(Channel channel, String clientId, String userName, boolean cleanSession, MqttPublishMessage willMessage) {
        this.clientId = clientId;
        this.userName = userName;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
    }

    public void addSub(Subscription sub) {
        subSet.add(sub);
    }

    public void removeSub(Subscription sub) {
        subSet.remove(sub);
    }

    public Set<Subscription> getAllSubInfo() {
        return subSet;
    }
}
