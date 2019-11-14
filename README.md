# jo-mqtt-server

#### 项目介绍
轻量级物联网MQTT服务器, 快速部署, 支持集群.

#### 软件架构说明
基于netty+springboot+redis+hazelcast技术栈实现
1. 使用netty实现通信及协议解析
2. 使用springboot提供依赖注入及属性配置,方便打包及快速部署
3. redis实现集群间消息存储（可自定义扩展）
4. hazelcast实现集群间消息通信（可自定义扩展）

#### 不支持
1. 暂且不支持websocket协议
2. 暂且不支持tcp-ssl配置
3. 不支持topic如下
    ```
    * 不支持为空
    * 不支持以'/'开始或结束
    * 不支持命名为'tRoot'
    * 不支持分隔符之间无字符 例如：ab///c
    * 不支持包含禁用字符 例如：ab/+11 或者c/12#1
    ```
#### 项目结构
```
joey-mqtt
  ├── mqtt-broker -- MQTT服务器功能的核心实现
  ├── mqtt-springboot -- springboot集成mqtt启动
  ├── mqtt-test -- MQTT服务器测试用例
```

#### 功能说明
1. 参考MQTT3.1.1规范实现
2. 完整的QoS服务质量等级实现
3. 遗嘱消息, 保留消息及消息分发重试
4. 心跳机制
5. 集群功能

#### 快速开始
- 本地运行
  - 代码clone到本地，启动MqttApplication程序即可，默认端口1883，配置详情：application-local.properties
- jar保运行（本地环境）
  - 代码clone到本地，修改application-production.properties的相关配置，用maven达成jar包
  - nohup java -jar -Dspring.profiles.active=local mqtt-springboot-1.0.0-SNAPSHOT.jar 2>&1 &

#### 日志文件配置
采用log4j2日志框架，可自行定义日志格式，修改log4j2.xml文件中相关配置即可

#### 集群使用
目前集群使用hazelcast的通信方式，使用者也可自定义
- hazelcast方式
  - 只需配置文件中mqtt.customConfig.hazelcastConfig.enable=true即可

#### 配置参数
```
#server config
mqtt.serverConfig.tcpPort=1883
#-1表示不开启
mqtt.serverConfig.webSocketPort=-1
mqtt.serverConfig.hostname=
mqtt.serverConfig.extendProviderClass=joey.mqtt.broker.provider.adapter.ExtendProviderAdapter
#mqtt.serverConfig.extendProviderClass=joey.mqtt.broker.provider.redis.RedisExtendProvider

#password 采用sha256hex加密 例子中密码明文和用户名一致
mqtt.serverConfig.enableAuth=true
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

#customer config
#hazelcast config
mqtt.customConfig.hazelcastConfig.enable=true
mqtt.customConfig.hazelcastConfig.configFilePath=classpath:hazelcast/hazelcast-local.xml
#mqtt.customConfig.hazelcastConfig.configFilePath=file:/home/hazelcast-local.xml

#redis config
mqtt.customConfig.redisConfig.host=172.16.32.177
mqtt.customConfig.redisConfig.password=
mqtt.customConfig.redisConfig.port=19000
mqtt.customConfig.redisConfig.database=0
mqtt.customConfig.redisConfig.timeout=3000
mqtt.customConfig.redisConfig.pool.maxActive=200
mqtt.customConfig.redisConfig.pool.maxWait=1000
mqtt.customConfig.redisConfig.pool.maxIdle=50
mqtt.customConfig.redisConfig.pool.minIdle=20

```

#### 自定义扩展
- 若当前功能不能满足用户需求可以自行扩展，使用者只需继承ExtendProviderAdapter复写相应的方法即可
  1. 修改配置文件mqtt.serverConfig.extendProviderClass=joey.mqtt.broker.provider.adapter.XXXXProvider
- 扩展方法说明（扩展接口：IExtendProvider）
  1. 获取messageId存储实现: IMessageIdStore initMessageIdStore();
  2. 获取session存储实现: ISessionStore initSessionStore();
  3. 获取主题订阅存储实现: ISubscriptionStore initSubscriptionStore(ISessionStore sessionStore);
  4. 获取retain消息存储实现: IRetainMessageStore initRetainMessageStore();
  5. 获取pubMessage消息存储实现: IDupPubMessageStore initDupPubMessageStore();
  6. 获取pubRelMessage消息存储实现: IDupPubRelMessageStore initDupPubRelMessageStore();
  7. 获取授权管理实现: IAuth initAuthManager(List<AuthUser> userList);
  8. 获取集群间通信实现: IInnerTraffic initInnerTraffic(InnerPublishEventProcessor innerPublishEventProcessor);
  9. 获取事件监听器列表: List<IEventListener> initEventListeners();
  
#### 参考实现
1. https://github.com/moquette-io/moquette
2. https://gitee.com/recallcode/iot-mqtt-server
3. https://github.com/Cicizz/jmqtt
4. https://github.com/Wizzercn/MqttWk
5. https://github.com/daoshenzzg/socket-mqtt
6. https://github.com/1ssqq1lxr/iot_push

#### 压测工具
1. https://github.com/takanorig/mqtt-bench 拷贝mqtt-test工程下mqtt-mock文件到测试机 执行即可
2. https://github.com/daoshenzzg/mqtt-mock

#### 工具推荐
1. https://github.com/looly/hutool Hutool是一个小而全的Java工具类库 作者有问必答 强烈推荐
2. http://www.tongxinmao.com/txm/webmqtt.php 在线mqtt测试
