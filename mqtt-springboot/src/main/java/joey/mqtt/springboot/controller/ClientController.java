package joey.mqtt.springboot.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import joey.mqtt.broker.MqttServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户controller
 *
 * @author Joey
 * @date 2019/9/20
 */
@RestController()
@RequestMapping("/api/client")
public class ClientController {
    @Resource
    private MqttServer mqttServer;

    @GetMapping("/info")
    public Object getInfo(@RequestParam String clientId) {
        JSONObject info = new JSONObject();

        info.put("clientSession", mqttServer.getClientInfoFor(clientId));
        info.put("clientSubSet", mqttServer.getClientSubInfoFor(clientId));

        return JSON.toJSONString(info);
    }
}
