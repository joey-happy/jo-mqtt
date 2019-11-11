package joey.mqtt.broker.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * netty 配置
 * @author Joey
 * @date 2019/7/18
 */
@Getter
@Setter
@ToString
public class NettyConfig {
    /**
     * 0 = current_processors_amount * 2
     */
    private int bossThreads = 0;

    /**
     * 0 = current_processors_amount * 2
     */
    private int workerThreads = 0;

    private boolean epoll = false;

    private int soBacklog = 1024;

    private boolean soReuseAddress = true;

    private boolean tcpNoDelay = true;

    private int soSndBuf = 65536;

    private int soRcvBuf = 65536;

    private boolean soKeepAlive = true;

    private int channelTimeoutSeconds = 200;

}
