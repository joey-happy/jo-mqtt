package joey.mqtt.springboot.controller;

import joey.mqtt.broker.MqttServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 统计controller
 *
 * @author Joey
 * @date 2019/9/20
 */
@RestController()
@RequestMapping("/api/stats")
public class StatsController {
    @Resource
    private MqttServer mqttServer;

    @GetMapping("/sessionCount")
    public Object getSessionCount() {
        return mqttServer.getSessionCount();
    }
}
