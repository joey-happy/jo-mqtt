# jo-mqtt-server

#### 项目介绍

轻量级物联网MQTT服务器, 快速部署, 支持集群.

#### 软件架构说明

基于java8+netty+springboot+redis+hazelcast技术栈实现

- 使用netty实现通信及协议解析
- 使用springboot提供依赖注入及属性配置,方便打包及快速部署
- 支持ssl,wss协议
- 自定义实现扩展
  1. MemoryExtendProvider实现，不支持集群间通信，只支持Qos为0等级的消息
  2. RedisExtendProvider实现，支持集群间通信(使用redis的pub sub实现)，支持Qos所有等级消息
  3. HazelcastExtendProvider实现，支持集群间通信，只支持Qos为0等级的消息
  4. 若以上3种不满足用户需求，可以自行扩展，修改配置文件mqtt.serverConfig.extendProviderClass=xxx.xxx.provider.XXXXProvider即可,可参考MemoryExtendProvider实现。同时也可以添加自定义变量，参考配置文件:用户自定义扩展配置

#### 不支持

不支持topic如下

```
* 不支持为空
* 不支持以'/'开始或结束
* 不支持命名为'joRootTopic'
* 不支持分隔符之间无字符 例如：ab///c
* 不支持包含禁用字符 例如：ab/+11 或者c/12#1
```

#### 项目结构

```
jo-mqtt
  ├── mqtt-broker -- MQTT服务器功能的核心实现
  ├── mqtt-springboot -- springboot集成mqtt启动
  ├── mqtt-test -- MQTT服务器测试用例
```

#### 功能说明

> 1. 参考MQTT3.1.1规范实现
> 2. 完整的QoS服务质量等级实现
> 3. 遗嘱消息, 保留消息及消息分发重试
> 4. 心跳机制
> 5. 集群功能

#### 快速开始

- 本地运行
  - 代码clone到本地，启动MqttApplication程序即可，tcp端口1883，tcp-ssl端口1888，websocket端口2883，websocket-ssl端口2888，配置详情：application-local.properties
- jar保运行（本地环境）
  - 参考deploy文件夹下的start.sh部署脚本

#### 日志文件配置

采用logback框架

#### 集群使用

集群默认使用RedisExtendProvider实现扩展，则集群间通信依赖redis的pubsub功能

#### 配置参数

```
#server config
#tcp端口配置
#-1表示不开启
mqtt.serverConfig.tcpPort=1883
#-1表示不开启
mqtt.serverConfig.tcpSslPort=1888

#webSocket配置
mqtt.serverConfig.webSocketPath=/joMqtt
#-1表示不开启
mqtt.serverConfig.webSocketPort=2883
#-1表示不开启
mqtt.serverConfig.webSocketSslPort=2888

mqtt.serverConfig.enableClientCA=false

mqtt.serverConfig.hostname=

#provider配置 默认有如下3中实现
#不支持集群间通信 不支持消息持久化
mqtt.serverConfig.extendProviderClass=joey.mqtt.broker.provider.MemoryExtendProvider

#支持集群间通信 支持消息持久化
#mqtt.serverConfig.extendProviderClass=joey.mqtt.broker.provider.RedisExtendProvider

#hazelcastProvider相关配置 支持集群间通信 不支持消息持久化
#mqtt.serverConfig.extendProviderClass=joey.mqtt.broker.provider.HazelcastExtendProvider
#mqtt.customConfig.hazelcastConfigFile=classpath:hazelcast/hazelcast-local.xml
#mqtt.customConfig.hazelcastConfigFile=file:/home/hazelcast-local.xml

#password 采用sha256hex加密 例子中密码明文和用户名一致
mqtt.serverConfig.enableUserAuth=true
mqtt.serverConfig.authUsers[0].userName=local
mqtt.serverConfig.authUsers[0].password=25bf8e1a2393f1108d37029b3df5593236c755742ec93465bbafa9b290bddcf6
mqtt.serverConfig.authUsers[1].userName=admin
mqtt.serverConfig.authUsers[1].password=8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918

#netty config
mqtt.nettyConfig.bossThreads=0
mqtt.nettyConfig.workerThreads=0
mqtt.nettyConfig.epoll=false
mqtt.nettyConfig.soBacklog=1024
mqtt.nettyConfig.soReuseAddress=true
mqtt.nettyConfig.tcpNoDelay=true
mqtt.nettyConfig.soSndBuf=65536
mqtt.nettyConfig.soRcvBuf=65536
mqtt.nettyConfig.soKeepAlive=true
mqtt.nettyConfig.channelTimeoutSeconds=200

#如果使用了RedisExtendProvider 则必须配置redisConfig
mqtt.customConfig.redisConfig.host=localhost
mqtt.customConfig.redisConfig.password=
mqtt.customConfig.redisConfig.port=6379
mqtt.customConfig.redisConfig.database=0
mqtt.customConfig.redisConfig.timeout=3000
mqtt.customConfig.redisConfig.pool.maxActive=50
mqtt.customConfig.redisConfig.pool.maxWait=1000
mqtt.customConfig.redisConfig.pool.maxIdle=50
mqtt.customConfig.redisConfig.pool.minIdle=20

# 如果开启ssl 则必须配置如下信息
# 建议使用：keytool -genkey -alias <desired certificate alias>
#                         -keystore <path to keystore.pfx>
#                         -storetype PKCS12
#                         -keyalg RSA
#                         -storepass <password>
#                         -validity 730
#                         -keysize 2048
mqtt.customConfig.sslContextConfig.sslKeyFilePath=ssl/jomqtt-server.pfx
mqtt.customConfig.sslContextConfig.sslKeyStoreType=PKCS12
mqtt.customConfig.sslContextConfig.sslManagerPwd=jo_mqtt
mqtt.customConfig.sslContextConfig.sslStorePwd=jo_mqtt

#自定义节点名称 可以不配置 默认是UUID
#mqtt.customConfig.nodeName=jo_mqtt_1

#用户自定义扩展配置
mqtt.customConfig.extConfig.k1=v1
mqtt.customConfig.extConfig.k2=v2
mqtt.customConfig.extConfig.k3.k31=v31
mqtt.customConfig.extConfig.k3.k32=v32
```

#### 自定义扩展

- 若当前功能不能满足用户需求可以自行扩展，使用者只需继承MemoryExtendProvider复写相应的方法，同时也可以添加自定义变量，参考配置文件:用户自定义扩展配置
  > 修改配置文件mqtt.serverConfig.extendProviderClass=joey.mqtt.broker.provider.XXXXProvider
  >
- 扩展方法说明（扩展接口：IExtendProvider）
  > 1. 获取messageId存储实现: IMessageIdStore initMessageIdStore();
  > 2. 获取session存储实现: ISessionStore initSessionStore();
  > 3. 获取主题订阅存储实现: ISubscriptionStore initSubscriptionStore(ISessionStore sessionStore);
  > 4. 获取retain消息存储实现: IRetainMessageStore initRetainMessageStore();
  > 5. 获取pubMessage消息存储实现: IDupPubMessageStore initDupPubMessageStore();
  > 6. 获取pubRelMessage消息存储实现: IDupPubRelMessageStore initDupPubRelMessageStore();
  > 7. 获取授权管理实现: IAuth initAuthManager(List<AuthUser> userList);
  > 8. 获取集群间通信实现: IInnerTraffic initInnerTraffic(InnerPublishEventProcessor innerPublishEventProcessor);
  > 9. 获取事件监听器列表: List<IEventListener> initEventListeners();
  > 10. 获取当前实例节点名称 String getNodeName();
  > 11. 初始化sslContext: SslContext initSslContext(boolean enableClientCA) throws Exception;
  >

#### 参考实现

1. https://github.com/moquette-io/moquette
2. https://gitee.com/recallcode/iot-mqtt-server
3. https://github.com/Cicizz/jmqtt
4. https://github.com/Wizzercn/MqttWk
5. https://github.com/daoshenzzg/socket-mqtt
6. https://github.com/1ssqq1lxr/iot_push
7. https://mp.weixin.qq.com/s/9ErXfPUnJk20bPi5rSzcWA
8. https://github.com/hivemq/hivemq-community-edition
9. https://github.com/lets-mica/mica-mqtt aio tio实现

#### 客户端工具

1. https://mqttfx.jensd.de/index.php/download 客户端工具下载
2. https://github.com/emqx/MQTTX/releases 客户端工具
3. http://www.emqx.io/online-mqtt-client/#/recent_connections 在线调试工具

#### 压测工具

> 1. https://github.com/takanorig/mqtt-bench 拷贝mqtt-test工程下mqtt-mock文件到测试机 执行即可
> 2. https://github.com/daoshenzzg/mqtt-mock ./mqtt-mock -broker "tcp://localhost:1883" -c 6000 -n 1000 -action sub -topic test -username local -password local -debug true

#### 工具推荐

1. https://github.com/looly/hutool Hutool是一个小而全的Java工具类库 作者有问必答 强烈推荐
2. http://www.tongxinmao.com/txm/webmqtt.php 在线mqtt测试
3. https://www.emqx.io/docs/zh/v4.3/tutorial/tune.html 系统调优
4. https://blog.csdn.net/Just_shunjian/article/details/78288229 Linux 内核优化-调大TCP最大连接数
5. https://github.com/xiaojiaqi/C1000kPracticeGuide C1000k优化实践

#### 作者微信（备注请标明jo-mqtt 方便时间内 有问必答）
![img](img/readme/author.png =100x100)
