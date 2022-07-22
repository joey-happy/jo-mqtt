package joey.mqtt.broker;

import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;
import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.util.ConfigUtils;

/**
 * 服务yml配置文件-测试
 *
 * @author Joey
 * @date 2022/7/22
 */
public class ServerWithYmlTest {
    public static void main(String[] args) throws Exception {
        Config config = ConfigUtils.loadFromYamlFile("config.yml");

        //用户自定义配置json 可以转换成自己的java对象
        JSONObject extConfJsonObj = config.getCustomConfig().convertExtConfig(JSONObject.class);
        Console.log(extConfJsonObj);

        MqttServer mqttServer = new MqttServer(config);
        mqttServer.start();
    }
}
