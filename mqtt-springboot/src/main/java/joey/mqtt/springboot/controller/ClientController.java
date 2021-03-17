package joey.mqtt.springboot.controller;

import com.alibaba.fastjson.JSON;
import joey.mqtt.broker.MqttServer;
import joey.mqtt.broker.core.client.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户controller
 *
 * @author Joey
 * @date 2019/9/20
 */
@RestController()
@RequestMapping("/api/client")
public class ClientController {
    @Autowired
    private MqttServer mqttServer;

    @GetMapping("/info")
    public Object getInfo(@RequestParam String clientId) {
        ClientSession clientSession = mqttServer.getClientInfoFor(clientId);

        return JSON.toJSONString(clientSession);
    }
}
