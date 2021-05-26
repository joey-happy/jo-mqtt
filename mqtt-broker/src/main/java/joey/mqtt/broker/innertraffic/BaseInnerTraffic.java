package joey.mqtt.broker.innertraffic;

import joey.mqtt.broker.core.message.CommonPublishMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

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

    private static final String THREAD_NAME_PRE = "joMqtt-innerTrafficExecutor-pool-";

    private static final int THREAD_CORE_SIZE = 20;

    private static final int THREAD_MAX_POOL_SIZE = 100;

    private static final long THREAD_KEEP_ALIVE_TIME = 1L;

    private static final AtomicLong THREAD_IDX = new AtomicLong();

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(getThreadName());
            return thread;
        }
    };

    private static final RejectedExecutionHandler REJECTED_EXECUTION_HANDLER = new ThreadPoolExecutor.DiscardPolicy() {
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor e) {
            // 继续执行任务
            Thread thread = new Thread(runnable);
            thread.setName(getThreadName());
            thread.start();
        }
    };

    private static String getThreadName() {
        return THREAD_NAME_PRE + THREAD_IDX.incrementAndGet();
    }

    private final ThreadPoolExecutor executor;

    public BaseInnerTraffic(String nodeName, InnerPublishEventProcessor innerPublishEventProcessor) {
        this.nodeName = nodeName;
        this.innerPublishEventProcessor = innerPublishEventProcessor;

        executor = new ThreadPoolExecutor(THREAD_CORE_SIZE,
                                          THREAD_MAX_POOL_SIZE,
                                          THREAD_KEEP_ALIVE_TIME,
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
