package joey.mqtt.pubsub.performance;

import java.util.concurrent.atomic.LongAdder;

/**
 * mqtt数据统计
 */
public class MqttCounter {
    public static LongAdder receiveCount = new LongAdder();

    public static LongAdder connectCount = new LongAdder();

    public static LongAdder connectFailCount = new LongAdder();

    public static LongAdder connectLostCount = new LongAdder();

    public static void addReceiveCount() {
        receiveCount.increment();
    }

    public static void addConnectCount() {
        connectCount.increment();
    }

    public static void addConnectFailCount() {
        connectFailCount.increment();
    }

    public static void addConnectLostCount() {
        connectLostCount.increment();
    }

    public static void clear() {
        receiveCount.reset();
        connectCount.reset();
        connectFailCount.reset();
        connectLostCount.reset();
    }

    public static String print(){
        StringBuilder sb = new StringBuilder();
        sb.append(" receiveCount = ").append(receiveCount);
        sb.append(" connectCount = ").append(connectCount);
        sb.append(" connectFailCount = ").append(connectFailCount);
        sb.append(" connectLostCount = ").append(connectLostCount);
        return sb.toString();
    }
}