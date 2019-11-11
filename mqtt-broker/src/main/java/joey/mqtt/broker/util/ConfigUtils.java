package joey.mqtt.broker.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.config.Config;

/**
 * 配置工具类
 *
 * @author Joey
 * @date 2019/9/9
 */
public class ConfigUtils {
    private ConfigUtils() {

    }

    /**
     * 从启动参数中加载配置文件 没有指定 则使用默认配置文件
     * 例如：java -Dmqtt.conf=XXXXX
     *
     * @param propsKey
     * @param defaultConfig
     * @return
     */
    public static Config loadFromSystemProps(String propsKey, Config defaultConfig) {
        String mqttConfigFilePath = System.getProperty(propsKey);

        //如果没有指定配置文件 则用默认配置
        if (StrUtil.isBlank(mqttConfigFilePath)) {
            return defaultConfig;
        }

        //如果指定配置文件 则加载配置
        Props props = Props.getProp(mqttConfigFilePath);
        return props.toBean(Config.class, Constants.MQTT_CONFIG_PROPS_PRE);
    }
}
