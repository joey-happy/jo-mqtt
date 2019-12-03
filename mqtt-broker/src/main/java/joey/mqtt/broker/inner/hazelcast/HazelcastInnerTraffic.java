package joey.mqtt.broker.inner.hazelcast;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.*;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.message.CommonPublishMessage;
import joey.mqtt.broker.exception.MqttException;
import joey.mqtt.broker.inner.IInnerTraffic;
import joey.mqtt.broker.inner.InnerPublishEventProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;

import static cn.hutool.core.util.URLUtil.CLASSPATH_URL_PREFIX;
import static cn.hutool.core.util.URLUtil.FILE_URL_PREFIX;

/**
 * hazelcast 实现集群间内部通信
 *
 * @author Joey
 * @date 2019/7/25
 */
@Slf4j
public class HazelcastInnerTraffic implements IInnerTraffic, MessageListener<CommonPublishMessage> {
    private final InnerPublishEventProcessor innerPublishEventProcessor;
    private final String configFile;
    private final String nodeName;

    private HazelcastInstance hzInstance;

    public HazelcastInnerTraffic(InnerPublishEventProcessor innerPublishEventProcessor, CustomConfig customConfig, String nodeName) {
        this.innerPublishEventProcessor = innerPublishEventProcessor;
        this.configFile = customConfig.getInnerTrafficConfig().getHazelcastConfigFile();
        this.nodeName = nodeName;

        initHazelcastInstance();

        //添加集群间topic监听
        ITopic<CommonPublishMessage> topic = hzInstance.getTopic(Constants.HAZELCAST_INNER_TRAFFIC_TOPIC);
        topic.addMessageListener(this);
    }

    /**
     * 初始化hazelcast实例
     */
    private void initHazelcastInstance() {
        if (StrUtil.isBlank(configFile)) {
            log.info("Hazelcast:use empty config.");
            hzInstance = Hazelcast.newHazelcastInstance();

        } else {
            try {
                Config hzConfig = null;

                if (configFile.startsWith(CLASSPATH_URL_PREFIX)) {
                    hzConfig = new ClasspathXmlConfig(configFile.substring(CLASSPATH_URL_PREFIX.length()));

                } else if (configFile.startsWith(FILE_URL_PREFIX)) {
                    hzConfig = new FileSystemXmlConfig(configFile.substring(FILE_URL_PREFIX.length()));
                }

                if (null == hzConfig) {
                    throw new MqttException("Hazelcast:config file path error. configFilePath=" + configFile);
                }

                log.info("Hazelcast:config file path={}", configFile);
                hzInstance = Hazelcast.newHazelcastInstance(hzConfig);

            } catch (FileNotFoundException e) {
                throw new MqttException("Hazelcast:could not find hazelcast config file. configFilePath=" + configFile);
            }
        }
    }

    /**
     * 发布消息
     * @param message
     */
    @Override
    public void publish(CommonPublishMessage message) {
        log.debug("HazelcastInnerTraffic-publish message={}", JSON.toJSONString(message));

        ITopic<CommonPublishMessage> topic = hzInstance.getTopic(Constants.HAZELCAST_INNER_TRAFFIC_TOPIC);
        topic.publish(message);
    }

    /**
     * 监听集群发送的消息
     * @param msg
     */
    @Override
    public void onMessage(Message<CommonPublishMessage> msg) {
        try {
            if (!msg.getPublishingMember().equals(hzInstance.getCluster().getLocalMember())) {
                CommonPublishMessage commonPubMsg = msg.getMessageObject();
                //集群间接收到消息 retain设置为false
                commonPubMsg.setRetain(false);

                log.info("Hazelcast:receive cluster message. message={}", commonPubMsg.toString());
                innerPublishEventProcessor.publish2Subscribers(commonPubMsg);
            }
        } catch (Exception ex) {
            log.error("Hazelcast:onMessage error. msg={}", msg, ex);
        }
    }
}
