package joey.mqtt.broker.core.dispatcher;

import io.netty.handler.codec.mqtt.MqttMessageType;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.constant.NumConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @Author：Joey
 * @Date: 2022/7/25
 * @Desc: 调度指挥中心
 **/
@Slf4j
public class DispatcherCommandCenter {
    private final Thread[] dispatcherExecutors;

    private final BlockingQueue<FutureTask<String>>[] dispatcherQueue;

    @Getter
    private final int dispatcherCount;

    private final int dispatcherQueueSize;

    public DispatcherCommandCenter(int dispatcherCount, int dispatcherQueueSize) {
        this.dispatcherCount = dispatcherCount;
        this.dispatcherQueueSize = dispatcherQueueSize;

        this.dispatcherQueue = new BlockingQueue[this.dispatcherCount];
        for (int i=NumConstants.INT_0; i<this.dispatcherCount; i++) {
            dispatcherQueue[i] = new ArrayBlockingQueue<>(this.dispatcherQueueSize);
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
     * @param clientId
     * @param messageType
     * @param action
     * @return
     */
    public DispatcherResult dispatch(String clientId, MqttMessageType messageType, final Callable<String> action) {
        return dispatch(clientId, messageType.toString(), action);
    }

    /**
     * 分发执行任务
     *
     * todo 返回值处理
     *
     * @param clientId
     * @param actionName
     * @param action
     * @return
     */
    public DispatcherResult dispatch(String clientId, String actionName, final Callable<String> action) {
        final DispatcherCommand command = new DispatcherCommand(clientId, action);
        final FutureTask<String> task = new FutureTask<>(() -> {
            command.execute();
            return command.getClientId();
        });

        int dispatcherIndex = Math.abs(clientId.hashCode()) % this.dispatcherCount;
        log.debug("DispatcherCommandCenter dispatch task. clientId={},actionName={},dispatcherIndex={}", clientId, actionName, dispatcherIndex);

        if (Thread.currentThread() == dispatcherExecutors[dispatcherIndex]) {
            DispatcherWorker.executeTask(task);
            return new DispatcherResult();
        }

        //todo 此处需要考虑如何处理
        if (this.dispatcherQueue[dispatcherIndex].offer(task)) {
            return new DispatcherResult();
        } else {
            return null;
        }
    }
}
