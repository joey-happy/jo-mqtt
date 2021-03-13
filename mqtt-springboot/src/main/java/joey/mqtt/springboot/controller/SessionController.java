package joey.mqtt.springboot.controller;

import joey.mqtt.broker.MqttServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * session controller
 *
 * @author Joey
 * @date 2019/9/20
 */
@RestController()
@RequestMapping("/api/session")
public class SessionController {
    @Autowired
    private MqttServer mqttServer;

    @GetMapping("/count")
    public Object count() {
        return mqttServer.sessionCount();
    }
}
