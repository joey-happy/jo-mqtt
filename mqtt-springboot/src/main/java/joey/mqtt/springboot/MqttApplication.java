package joey.mqtt.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * spring-boot启动类
 *
 * @author Joey
 * @date 2019/09/02
 */
@SpringBootApplication(scanBasePackages = {"joey.mqtt.springboot"})
public class MqttApplication {
    public static void main(String[] args) {
        SpringApplication.run(MqttApplication.class, args);
    }
}
