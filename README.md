# jo-mqtt-server

#### 项目介绍
轻量级物联网MQTT服务器, 快速部署, 支持集群.

#### 软件架构说明
基于netty+springboot+redis+hazelcast技术栈实现
1. 使用netty实现通信及协议解析
2. 使用springboot提供依赖注入及属性配置
3. redis实现集群间消息存储
4. hazelcast实现集群间消息通信

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
- jar保运行（生产环境）
  - 代码clone到本地，修改application-production.properties的相关配置，用maven达成jar包
  - 执行命令：java -jar -Dspring.profiles.active=production mqtt-springboot-1.0.0-SNAPSHOT.jar

#### 日志文件配置
采用log4j2日志框架，可自行定义日志格式，修改log4j2.xml文件中相关配置即可

#### 集群使用
目前集群使用hazelcast的通信方式，使用者也可自定义
- hazelcast方式
  - 只需配置文件中mqtt.customConfig.hazelcastConfig.enable=true即可

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