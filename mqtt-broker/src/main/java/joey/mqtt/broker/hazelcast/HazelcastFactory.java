package joey.mqtt.broker.hazelcast;

import cn.hutool.core.util.StrUtil;
import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import joey.mqtt.broker.exception.MqttException;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;

import static cn.hutool.core.util.URLUtil.CLASSPATH_URL_PREFIX;
import static cn.hutool.core.util.URLUtil.FILE_URL_PREFIX;

/**
 * hazelcast工厂
 *
 * @author Joey
 * @date 2021-03-19
 */
@Slf4j
public class HazelcastFactory {
    private HazelcastFactory() {

    }

    /**
     * 创建hazelcast实例
     */
    public static HazelcastInstance createInstance() {
        return createInstance(StrUtil.EMPTY);
    }

    /**
     * 创建hazelcast实例
     */
    public static HazelcastInstance createInstance(String configFile) {
        if (StrUtil.isBlank(configFile)) {
            log.info("Hazelcast:use empty config.");
            return Hazelcast.newHazelcastInstance();
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

                log.info("Hazelcast:config file path={},config={}.", configFile, hzConfig);
                return Hazelcast.newHazelcastInstance(hzConfig);

            } catch (FileNotFoundException e) {
                throw new MqttException("Hazelcast:could not find hazelcast config file. configFilePath=" + configFile);
            }
        }
    }
}
