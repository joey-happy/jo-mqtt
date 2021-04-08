package joey.mqtt.springboot.task;

import joey.mqtt.broker.MqttServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 统计任务
 * @author Joey
 * @date 2021-04-08
 */
@Component
@Slf4j
public class StatsTask {
    @Autowired
    private MqttServer mqttServer;

    /**
     * 打印当前连接session数量
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void statSessionCount() {
        int sessionCount = mqttServer.getSessionCount();
        log.info("StatsTask-statSessionCount count={}.", sessionCount);
    }
}
