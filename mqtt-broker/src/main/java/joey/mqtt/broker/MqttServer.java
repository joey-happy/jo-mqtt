package joey.mqtt.broker;


import cn.hutool.core.util.ClassLoaderUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;
import joey.mqtt.broker.codec.MqttWebSocketCodec;
import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.config.NettyConfig;
import joey.mqtt.broker.config.ServerConfig;
import joey.mqtt.broker.core.MqttMaster;
import joey.mqtt.broker.handler.MqttMainHandler;
import joey.mqtt.broker.provider.IExtendProvider;
import joey.mqtt.broker.util.ConfigUtils;
import joey.mqtt.broker.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * mqtt server
 * 文档：http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csprd02/mqtt-v3.1.1-csprd02.html#_Toc385349839
 *      https://mcxiaoke.gitbooks.io/mqtt-cn/content/
 *
 * @author Joey
 * @date 2019/7/18
 */
@Slf4j
public class MqttServer {
    private final Config config;

    private final ServerConfig serverConfig;
    private final NettyConfig nettyConfig;
    private final CustomConfig customConfig;
    private IExtendProvider extendProvider;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Class<? extends ServerSocketChannel> channelClass;
    private Channel tcpChannel;
    private Channel webSocketChannel;

    private MqttMaster mqttMaster;

    public MqttServer(Config config) throws Exception {
        this.config = config;

        this.serverConfig = config.getServerConfig();
        this.nettyConfig = config.getNettyConfig();
        this.customConfig = config.getCustomConfig();
        CustomConfig copyCustomConfig = JSONObject.parseObject(JSON.toJSONString(customConfig), CustomConfig.class);

        //反射加载provider实现类 此实现类必须有一个含有参数'CustomConfig'的构造方法
        this.extendProvider = ClassLoaderUtil.loadClass(serverConfig.getExtendProviderClass())
                                             .asSubclass(IExtendProvider.class)
                                             .getConstructor(CustomConfig.class)
                                             .newInstance(copyCustomConfig);
    }

    /**
     * 启动服务
     */
    public void start() throws Exception {
        mqttMaster = new MqttMaster(config, extendProvider);
        final MqttMainHandler mqttMainHandler = new MqttMainHandler(mqttMaster);

        initGroups();

        tcpChannel = startServer(mqttMainHandler, Constants.ServerProtocolType.TCP, serverConfig.getTcpPort());

        webSocketChannel = startServer(mqttMainHandler, Constants.ServerProtocolType.WEB_SOCKET, serverConfig.getWebSocketPort());

        /**
         * 添加钩子方法
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
    }

    private Channel startServer(MqttMainHandler mqttMainHandler, Constants.ServerProtocolType protocolType, int port) {
        if (port <= Constants.INT_ZERO) {
            return null;
        }

        ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
                                                        .channel(channelClass)
                                        //              .handler(new LoggingHandler(LogLevel.INFO))
                                                        .childHandler(new ChannelInitializer() {
                                                            @Override
                                                            protected void initChannel(Channel channel) throws Exception {
                                                                ChannelPipeline pipeline = channel.pipeline();

                                                                //心跳检测
                                                                pipeline.addFirst(Constants.HANDLER_IDLE_STATE, new IdleStateHandler(0, 0, nettyConfig.getChannelTimeoutSeconds()));

                                                                //webSocket协议
                                                                if (Constants.ServerProtocolType.WEB_SOCKET == protocolType) {
                                                                    // 将请求和应答消息编码或解码为HTTP消息
                                                                    pipeline.addLast("http-codec", new HttpServerCodec());
                                                                    // 将HTTP消息的多个部分合成一条完整的HTTP消息
                                                                    pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
                                                                    // 将HTTP消息进行压缩编码
                                                                    pipeline.addLast("compressor ", new HttpContentCompressor());
                                                                    pipeline.addLast("protocol", new WebSocketServerProtocolHandler(serverConfig.getWebSocketPath(), Constants.MQTT_SUB_PROTOCOL_CSV_LIST, true, 65536));
                                                                    pipeline.addLast("mqttWebSocketCodec", new MqttWebSocketCodec());
                                                                }

                                                                //mqtt解码编码
                                                                pipeline.addLast(Constants.HANDLER_MQTT_DECODER, new MqttDecoder());
                                                                pipeline.addLast(Constants.HANDLER_MQTT_ENCODER, MqttEncoder.INSTANCE);

                                                                //mqtt操作handler
                                                                pipeline.addLast(Constants.HANDLER_MQTT_MAIN, mqttMainHandler);
                                                            }
                                                        });

        initConnectionOptions(bootstrap);

        InetSocketAddress socketAddress = new InetSocketAddress(port);
        if (!StringUtil.isNullOrEmpty(serverConfig.getHostname())) {
            socketAddress = new InetSocketAddress(serverConfig.getHostname(), port);
        }

        ChannelFuture channelFuture = bootstrap.bind(socketAddress).addListener((future) -> {
                                            if (future.isSuccess()) {
                                                log.info(protocolType.name + " server started at port: {}", port);

                                            } else {
                                                log.error(protocolType.name + " server start failed at port: {}!, errMsg={}", port, future.cause().getMessage());
                                            }
                                      });

        return channelFuture.channel();
    }

    /**
     * 初始化工作线程组
     */
    protected void initGroups() {
        if (nettyConfig.isEpoll()) {
            channelClass = EpollServerSocketChannel.class;
            bossGroup = new EpollEventLoopGroup(nettyConfig.getBossThreads());
            workerGroup = new EpollEventLoopGroup(nettyConfig.getWorkerThreads());

        } else {
            channelClass = NioServerSocketChannel.class;
            bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreads());
            workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreads());
        }
    }

    /**
     * 初始化socket连接参数
     * @param bootstrap
     */
    protected void initConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.childOption(ChannelOption.TCP_NODELAY, nettyConfig.isTcpNoDelay());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.isSoKeepAlive());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, nettyConfig.getSoRcvBuf());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, nettyConfig.getSoSndBuf());

        bootstrap.option(ChannelOption.SO_REUSEADDR, nettyConfig.isSoReuseAddress());
        bootstrap.option(ChannelOption.SO_BACKLOG, nettyConfig.getSoBacklog());
    }

    /**
     * 停止服务
     */
    public void stop() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();

        if (null != tcpChannel) {
            tcpChannel.closeFuture().syncUninterruptibly();
        }

        if (null != webSocketChannel) {
            webSocketChannel.closeFuture().syncUninterruptibly();
        }

        mqttMaster.close();

        log.info("Server stopped");
    }

    /**
     * session数量
     * @return
     */
    public long sessionCount() {
        return mqttMaster.sessionCount();
    }

    /**
     * mqtt-server main方法
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Stopwatch start = Stopwatch.start();

        new MqttServer(ConfigUtils.loadFromSystemProps(Constants.MQTT_CONFIG, new Config())).start();

        log.info("MqttServer-start. timeCost={}ms", start.elapsedMills());
    }
}
