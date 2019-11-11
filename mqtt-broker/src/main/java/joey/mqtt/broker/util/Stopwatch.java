package joey.mqtt.broker.util;

/**
 * 2019-09-17 Joey
 *
 * 简单计时
 */
public class Stopwatch {
    private long startTime = 0L;

    private long elapsedMills = 0L;

    private boolean isRunning;

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
        this.elapsedMills = 0L;
        this.isRunning = false;

        return this;
    }

    public long elapsedMills() {
        return this.isRunning ? System.currentTimeMillis() - startTime : this.elapsedMills;
    }

    public long elapsedSeconds() {
        return this.isRunning ? ((System.currentTimeMillis() - startTime) /1000) : (this.elapsedMills / 1000);
    }
}
