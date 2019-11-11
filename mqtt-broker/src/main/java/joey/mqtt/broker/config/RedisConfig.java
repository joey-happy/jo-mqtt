package joey.mqtt.broker.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * redis配置
 * @author Joey
 * @date 2019/9/7
 */
@Getter
@Setter
@ToString
public class RedisConfig {
    private String host;

    private String password;

    private int port;

    private int database;

    private int timeout = 3000;

    private Pool pool;

    @Data
    public static class Pool {

        /**
         * Max number of "idle" connections in the pool. Use a negative value to indicate
         * an unlimited number of idle connections.
         */
        private int maxIdle = 50;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if it is positive.
         */
        private int minIdle = 10;

        /**
         * Max number of connections that can be allocated by the pool at a given time.
         * Use a negative value for no limit.
         */
        private int maxActive = 200;

        /**
         * Maximum amount of time (in milliseconds) a connection allocation should block
         * before throwing an exception when the pool is exhausted. Use a negative value
         * to block indefinitely.
         */
        private int maxWait = 1000;

        private long minEvictableIdleTimeMillis = 5 * 60 * 1000L;

        /**
         * 有两个含义：
         * <br>
         * 1) 检查连接的间隔时间，如果连接空闲时间大于等于minEvictableIdleTimeMillis则关闭物理连接。
         * <br>
         * 2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
         */
        private long timeBetweenEvictionRunsMillis = 60 * 1000L;

        /**
         * 申请连接的时候，如果空闲时间大于timeBetweenEvictionRunsMillis，则检查
         */
        private boolean testWhileIdle = true;

        /**
         * 向资源池借用连接时是否做连接有效性检测(ping)，无效连接会被移除
         */
        private boolean testOnBorrow = false;
    }
}
