package joey.mqtt.broker.core.dispatcher;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;

/**
 * @Author：Joey
 * @Date: 2022/7/25
 * @Desc: 分发任务
 **/
@Slf4j
public class DispatcherWorker implements Runnable {
    private final BlockingQueue<FutureTask<String>> dispatcherQueue;

    public DispatcherWorker(BlockingQueue<FutureTask<String>> dispatcherQueue) {
        this.dispatcherQueue = dispatcherQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final FutureTask<String> task = this.dispatcherQueue.take();
                executeTask(task);

            } catch (InterruptedException e) {
                log.warn("Dispatcher task interrupted. threadName={}", Thread.currentThread().getName());
                break;
            }
        }

        log.warn("Dispatcher task exit. threadName={}", Thread.currentThread().getName());
    }

    public static void executeTask(final FutureTask<String> task) {
        if (!task.isCancelled()) {
            try {
                task.run();

                //阻塞 等待结果
                task.get();
            } catch (Throwable ex) {
                log.error("Dispatcher task executed error. threadName={}", Thread.currentThread().getName(), ex);
            }
        }
    }
}
