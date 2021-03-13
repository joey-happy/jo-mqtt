package joey.mqtt.broker.innertraffic;

import joey.mqtt.broker.core.message.CommonPublishMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 集群间通信基类
 *
 * @author Joey
 * @date 2021/03/13
 */
@Slf4j
public abstract class BaseInnerTraffic implements IInnerTraffic {
    protected final String nodeName;

    protected final InnerPublishEventProcessor innerPublishEventProcessor;

    private static final String THREAD_NAME_PRE = "InnerTraffic-thread-";

    private static final int CORE_SIZE = 20;

    private static final int MAX_POOL_SIZE = 100;

    private static final long KEEP_ALIVE_TIME = 1L;

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        final AtomicInteger idx = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME_PRE + idx.incrementAndGet());
            return thread;
        }
    };

    private static final RejectedExecutionHandler REJECTED_EXECUTION_HANDLER = new ThreadPoolExecutor.DiscardPolicy() {
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor e) {
            // 继续执行任务
            Thread thread = new Thread(runnable);
            thread.start();
        }
    };

    private final ThreadPoolExecutor executor;

    public BaseInnerTraffic(String nodeName, InnerPublishEventProcessor innerPublishEventProcessor) {
        this.nodeName = nodeName;
        this.innerPublishEventProcessor = innerPublishEventProcessor;

        executor = new ThreadPoolExecutor(CORE_SIZE,
                                          MAX_POOL_SIZE,
                                          KEEP_ALIVE_TIME,
                                          TimeUnit.HOURS,
                                          new SynchronousQueue<>(),
                                          THREAD_FACTORY,
                                          REJECTED_EXECUTION_HANDLER);
    }

    protected void publish2Subscribers(CommonPublishMessage commonPubMsg) {
        executor.execute( () -> {
            try {
                innerPublishEventProcessor.publish2Subscribers(commonPubMsg);
            } catch (Throwable t) {
                log.error("RedisInnerTraffic-onMessage error. nodeName={},message={}", nodeName, commonPubMsg, t);
            }
        });
    }
}
