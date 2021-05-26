package joey.mqtt.broker.config;

import lombok.*;

/**
 * mqtt-broker配置
 *
 * @author Joey
 * @date 2019/7/18
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private ServerConfig serverConfig = new ServerConfig();

    private NettyConfig nettyConfig = new NettyConfig();

    private CustomConfig customConfig = new CustomConfig();
}
