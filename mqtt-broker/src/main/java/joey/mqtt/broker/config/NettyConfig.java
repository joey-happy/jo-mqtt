package joey.mqtt.broker.config;

import joey.mqtt.broker.constant.NumConstants;
import lombok.Data;

/**
 * netty 配置
 * @author Joey
 * @date 2019/7/18
 */
@Data
public class NettyConfig {
    /**
     * 0 = current_processors_amount * 2
     */
    private int bossThreads = NumConstants.INT_0;

    /**
     * 0 = current_processors_amount * 2
     */
    private int workerThreads = NumConstants.INT_0;

    private boolean epoll = false;

    private int soBacklog = NumConstants.INT_1024;

    private boolean soReuseAddress = true;

    private boolean tcpNoDelay = true;

    private int soSndBuf = NumConstants.INT_65536;

    private int soRcvBuf = NumConstants.INT_65536;

    private boolean soKeepAlive = true;

    private int channelTimeoutSeconds = NumConstants.INT_100;

}
