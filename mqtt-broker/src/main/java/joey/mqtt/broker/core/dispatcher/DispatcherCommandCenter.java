package joey.mqtt.broker.core.dispatcher;

import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.constant.NumConstants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @Author：Joey
 * @Date: 2022/7/25
 * @Desc: 调度指挥中心
 **/
public class DispatcherCommandCenter {
    private Thread[] dispatcherExecutors;

    private BlockingQueue<FutureTask<String>>[] dispatcherQueue;

    private int dispatcherCount;

    private int dispatcherQueueSize;

    public DispatcherCommandCenter(int dispatcherCount, int dispatcherQueueSize) {
        this.dispatcherCount = dispatcherCount;
        this.dispatcherQueueSize = dispatcherQueueSize;

        this.dispatcherQueue = new BlockingQueue[this.dispatcherCount];
        for (int i=NumConstants.INT_0; i<this.dispatcherCount; i++) {
            dispatcherQueue[i] = new ArrayBlockingQueue<>(dispatcherQueueSize);
        }

        this.dispatcherExecutors = new Thread[this.dispatcherCount];
        for (int i=NumConstants.INT_0; i<this.dispatcherCount; i++) {
            this.dispatcherExecutors[i] = new Thread(new DispatcherWorker(dispatcherQueue[i]));
            this.dispatcherExecutors[i].setName(BusinessConstants.MQTT_DISPATCHER_THREAD_NAME_PRE + i);
            this.dispatcherExecutors[i].start();
        }
    }

    /**
     * 分发执行任务
     *
     * todo 返回值处理 添加日志
     *
     * @param clientId
     * @param action
     * @return
     */
    public DispatcherResult dispatch(String clientId, final Callable<String> action) {
        final DispatcherCommand command = new DispatcherCommand(clientId, action);
        final FutureTask<String> task = new FutureTask<>(() -> {
            command.execute();
            command.complete();
            return command.getClientId();
        });

        int targetDispatcherIndex = Math.abs(clientId.hashCode()) % this.dispatcherCount;
        if (Thread.currentThread() == dispatcherExecutors[targetDispatcherIndex]) {
            DispatcherWorker.executeTask(task);
            return new DispatcherResult();
        }

        if (this.dispatcherQueue[targetDispatcherIndex].offer(task)) {
            return new DispatcherResult();
        } else {
            return null;
        }
    }
}
