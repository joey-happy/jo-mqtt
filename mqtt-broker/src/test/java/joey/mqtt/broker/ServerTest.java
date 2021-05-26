package joey.mqtt.broker;

import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson.JSONObject;
import joey.mqtt.broker.config.Config;

/**
 * 服务测试
 *
 * @author Joey
 * @date 2021-05-26
 */
public class ServerTest {
    public static void main(String[] args) throws Exception {
        //如果指定配置文件 则加载配置
        Props props = Props.getProp("config.properties");
        Config config = props.toBean(Config.class, Constants.MQTT_CONFIG_PROPS_PRE);

        //用户自定义配置json 可以转换成自己的java对象
        String extConfJsonStr = JSONObject.toJSONString(config.getCustomConfig().getExtConf());

        MqttServer mqttServer = new MqttServer(config);
        mqttServer.start();
    }
}
