package joey.mqtt.broker.util;

import joey.mqtt.broker.constant.NumConstants;

/**
 * 简单计时功能实现
 *
 * @author Joey
 * @date 2019-09-17
 *
 */
public class Stopwatch {
    private Long startTime = NumConstants.LONG_0;

    private Long elapsedMills = NumConstants.LONG_0;

    private volatile Boolean isRunning;

    private Stopwatch() {
    }

    public static Stopwatch start() {
        Stopwatch stopWatch = new Stopwatch();

        stopWatch.startTime = System.currentTimeMillis();
        stopWatch.isRunning = true;

        return stopWatch;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public Stopwatch stop() {
        this.isRunning = false;
        this.elapsedMills = System.currentTimeMillis() - startTime;

        return this;
    }

    public Stopwatch reset() {
        this.elapsedMills = NumConstants.LONG_0;
        this.isRunning = false;

        return this;
    }

    public long elapsedMills() {
        return this.isRunning ? System.currentTimeMillis() - startTime : this.elapsedMills;
    }

    public long elapsedSeconds() {
        return this.isRunning ? ((System.currentTimeMillis() - startTime) / NumConstants.INT_1000) : (this.elapsedMills / NumConstants.INT_1000);
    }
}
