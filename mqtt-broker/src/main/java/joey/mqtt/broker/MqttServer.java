package joey.mqtt.broker;

import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import joey.mqtt.broker.codec.MqttWebSocketCodec;
import joey.mqtt.broker.config.Config;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.config.NettyConfig;
import joey.mqtt.broker.config.ServerConfig;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.constant.NumConstants;
import joey.mqtt.broker.core.MqttMaster;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.enums.ServerProtocolTypeEnum;
import joey.mqtt.broker.exception.MqttException;
import joey.mqtt.broker.handler.MqttMainHandler;
import joey.mqtt.broker.provider.IExtendProvider;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;
import java.util.Set;

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
    private Channel tcpSslChannel;

    private Channel webSocketChannel;
    private Channel webSocketSslChannel;

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

        boolean startedFlag = false;

        //tcp启动
        int tcpPort = serverConfig.getTcpPort();
        if (tcpPort > NumConstants.INT_0) {
            tcpChannel = startServer(mqttMainHandler, ServerProtocolTypeEnum.TCP, tcpPort, false);
            startedFlag = true;
        }

        //tcp-ssl启动
        int tcpSslPort = serverConfig.getTcpSslPort();
        if (tcpSslPort > NumConstants.INT_0) {
            tcpSslChannel = startServer(mqttMainHandler, ServerProtocolTypeEnum.TCP, tcpSslPort, true);
            startedFlag = true;
        }

        //websocket启动
        int webSocketPort = serverConfig.getWebSocketPort();
        if (webSocketPort > NumConstants.INT_0) {
            webSocketChannel = startServer(mqttMainHandler, ServerProtocolTypeEnum.WEB_SOCKET, webSocketPort, false);
            startedFlag = true;
        }

        //websocket-ssl启动
        int webSocketSslPort = serverConfig.getWebSocketSslPort();
        if (webSocketSslPort > NumConstants.INT_0) {
            webSocketSslChannel = startServer(mqttMainHandler, ServerProtocolTypeEnum.WEB_SOCKET, webSocketSslPort, true);
            startedFlag = true;
        }

        if (!startedFlag) {
            stop();
            throw new MqttException("Mqtt-server 启动失败, 请设置有效的端口号.");
        }

        /**
         * 添加钩子方法
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
    }

    private Channel startServer(MqttMainHandler mqttMainHandler, ServerProtocolTypeEnum protocolType, int port, boolean useSsl) {
        ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
                                                        .channel(channelClass)
                                        //              .handler(new LoggingHandler(LogLevel.INFO))
                                                        .childHandler(new ChannelInitializer() {
                                                            @Override
                                                            protected void initChannel(Channel channel) throws Exception {
                                                                ChannelPipeline pipeline = channel.pipeline();

                                                                //心跳检测
                                                                pipeline.addFirst(BusinessConstants.HANDLER_IDLE_STATE, new IdleStateHandler(NumConstants.INT_0, NumConstants.INT_0, nettyConfig.getChannelTimeoutSeconds()));

                                                                //添加ssl handler
                                                                addSslHandler(channel, pipeline, useSsl);

                                                                //添加webSocket handler
                                                                addWebSocketHandler(pipeline, protocolType);

                                                                //mqtt解码编码
                                                                pipeline.addLast(BusinessConstants.HANDLER_MQTT_DECODER, new MqttDecoder());
                                                                pipeline.addLast(BusinessConstants.HANDLER_MQTT_ENCODER, MqttEncoder.INSTANCE);

                                                                //mqtt操作handler
                                                                pipeline.addLast(BusinessConstants.HANDLER_MQTT_MAIN, mqttMainHandler);
                                                            }
                                                        });

        initConnectionOptions(bootstrap);

        InetSocketAddress socketAddress = new InetSocketAddress(port);
        if (StrUtil.isNotBlank(serverConfig.getHostname())) {
            socketAddress = new InetSocketAddress(serverConfig.getHostname(), port);
        }

        ChannelFuture channelFuture = bootstrap.bind(socketAddress)
                                               .addListener(future -> {
                                                    if (future.isSuccess()) {
                                                        log.info(protocolType.name + " server started at port={} useSsl={}", port, useSsl);

                                                    } else {
                                                        log.error(protocolType.name + " server start failed at port={} useSsl={} errMsg={}", port, useSsl, future.cause().getMessage());
                                                    }
                                              });

        return channelFuture.channel();
    }

    /**
     * 添加ssl handler
     *
     * @param channel
     * @param pipeline
     * @param useSsl
     * @throws Exception
     */
    private void addSslHandler(Channel channel, ChannelPipeline pipeline, boolean useSsl) throws Exception {
        if (useSsl) {
            boolean enableClientCA = serverConfig.isEnableClientCA();
            pipeline.addLast(BusinessConstants.HANDLER_SSL, buildSslHandler(channel.alloc(), extendProvider.initSslContext(enableClientCA), enableClientCA));
        }
    }

    /**
     * 添加webSocket handler
     *
     * @param pipeline
     * @param serverProtocolType
     */
    private void addWebSocketHandler(ChannelPipeline pipeline, ServerProtocolTypeEnum serverProtocolType) {
        if (ObjectUtil.equal(ServerProtocolTypeEnum.WEB_SOCKET, serverProtocolType)) {
            // 将请求和应答消息编码或解码为HTTP消息
            pipeline.addLast(BusinessConstants.HANDLER_HTTP_CODEC, new HttpServerCodec());

            // 将HTTP消息的多个部分合成一条完整的HTTP消息
            pipeline.addLast(BusinessConstants.HANDLER_HTTP_AGGREGATOR, new HttpObjectAggregator(NumConstants.INT_1048576));

            // 将HTTP消息进行压缩编码
            pipeline.addLast(BusinessConstants.HANDLER_HTTP_COMPRESSOR, new HttpContentCompressor());

            pipeline.addLast(BusinessConstants.HANDLER_WEB_SOCKET_SERVER_PROTOCOL, new WebSocketServerProtocolHandler(serverConfig.getWebSocketPath(), BusinessConstants.MQTT_SUB_PROTOCOL_CSV_LIST, true, NumConstants.INT_65536));
            pipeline.addLast(BusinessConstants.HANDLER_MQTT_WEB_SOCKET_CODEC, new MqttWebSocketCodec());
        }
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
     *
     * @param bootstrap
     */
    protected void initConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.childOption(ChannelOption.TCP_NODELAY, nettyConfig.isTcpNoDelay());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.isSoKeepAlive());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, nettyConfig.getSoRcvBuf());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, nettyConfig.getSoSndBuf());

        bootstrap.option(ChannelOption.SO_REUSEADDR, nettyConfig.isSoReuseAddress());
        bootstrap.option(ChannelOption.SO_BACKLOG, nettyConfig.getSoBacklog());

        //todo 此参数需要研究 目前测试 影响不大
//        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
//        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    /**
     * 构建ssl-handler
     *
     * @param alloc
     * @param sslContext
     * @param needClientAuth
     * @return
     */
    private ChannelHandler buildSslHandler(ByteBufAllocator alloc, SslContext sslContext, boolean needClientAuth) {
        SSLEngine sslEngine = sslContext.newEngine(alloc);

        //服务端模式
        sslEngine.setUseClientMode(false);

        //是否验证客户端
        if (needClientAuth) {
            sslEngine.setNeedClientAuth(true);
        }

        return new SslHandler(sslEngine);
    }

    /**
     * 停止服务
     */
    public void stop() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();

        if (ObjectUtil.isNotNull(tcpChannel)) {
            log.info("Close tcp channel.");
            tcpChannel.closeFuture().syncUninterruptibly();
        }

        if (ObjectUtil.isNotNull(tcpSslChannel)) {
            log.info("Close tcp ssl channel.");
            tcpSslChannel.closeFuture().syncUninterruptibly();
        }

        if (ObjectUtil.isNotNull(webSocketChannel)) {
            log.info("Close web socket channel.");
            webSocketChannel.closeFuture().syncUninterruptibly();
        }

        if (ObjectUtil.isNotNull(webSocketSslChannel)) {
            log.info("Close web socket ssl channel.");
            webSocketSslChannel.closeFuture().syncUninterruptibly();
        }

        mqttMaster.close();
        log.info("Server stopped.");
    }

    /**
     * session数量
     *
     * @return
     */
    public int getSessionCount() {
        return mqttMaster.getSessionCount();
    }

    /**
     * 获取client信息
     *
     * @param clientId
     */
    public ClientSession getClientInfoFor(String clientId) {
        return mqttMaster.getClientInfoFor(clientId);
    }

    /**
     * 获取client订阅信息
     *
     * @param clientId
     * @return
     */
    public Set<Subscription> getClientSubInfoFor(String clientId) {
        return mqttMaster.getClientSubInfoFor(clientId);
    }
}
