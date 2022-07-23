package joey.mqtt.broker.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.yaml.YamlUtil;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.config.YamlWrapperConfig;

/**
 * 配置工具类
 *
 * @author Joey
 * @date 2022/7/22
 */
public class ConfigUtils {
    private static final String PROPS_SUFFIX = "properties";

    private static final String YAML_SUFFIX = "yml";

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
        String mqttConfigFile = System.getProperty(propsKey);

        if (StrUtil.isBlank(mqttConfigFile)) {
            return defaultConfig;
        }

        if (ObjectUtil.equals(PROPS_SUFFIX, FileUtil.getSuffix(mqttConfigFile))) {
            return loadFromPropertiesFile(mqttConfigFile);
        }

        if (ObjectUtil.equals(YAML_SUFFIX, FileUtil.getSuffix(mqttConfigFile))) {
            return loadFromYamlFile(mqttConfigFile);
        }

        return null;
    }

    /**
     * 从properties文件加载配置
     *
     * @param mqttConfigFile
     * @return
     */
    public static Config loadFromPropertiesFile(String mqttConfigFile) {
        Props props = Props.getProp(mqttConfigFile, CharsetUtil.CHARSET_UTF_8);
        return props.toBean(Config.class, BusinessConstants.MQTT_CONFIG_PROPS_PRE);
    }

    /**
     * 从yaml文件加载配置
     *
     * @param mqttConfigFile
     * @return
     */
    public static Config loadFromYamlFile(String mqttConfigFile) {
        YamlWrapperConfig yamlWrapperConfig = YamlUtil.loadByPath(mqttConfigFile, YamlWrapperConfig.class);
        return yamlWrapperConfig.getMqtt();
    }

    public static void main(String[] args) {
        Config config = ConfigUtils.loadFromSystemProps(BusinessConstants.MQTT_CONFIG, new Config());
        Console.log(config);
    }
}
