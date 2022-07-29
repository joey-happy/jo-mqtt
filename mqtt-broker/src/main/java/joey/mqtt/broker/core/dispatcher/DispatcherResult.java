package joey.mqtt.broker.core.dispatcher;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @Author：Joey
 * @Date: 2022/7/25
 * @Desc: 分发结果
 *
 * 参考: moquette PostOffice.RouteResult
 **/
@Slf4j
public class DispatcherResult {
    private final String clientId;

    private final Status status;

    private CompletableFuture queuedFuture;

    enum Status {SUCCESS, FAIL}

    public static DispatcherResult success(String clientId, CompletableFuture queuedFuture) {
        return new DispatcherResult(clientId, Status.SUCCESS, queuedFuture);
    }

    public static DispatcherResult failed(String clientId) {
        return failed(clientId, null);
    }

    public static DispatcherResult failed(String clientId, String error) {
        final CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.completeExceptionally(new Error(error));
        return new DispatcherResult(clientId, Status.FAIL, failed);
    }

    private DispatcherResult(String clientId, Status status, CompletableFuture queuedFuture) {
        this.clientId = clientId;
        this.status = status;
        this.queuedFuture = queuedFuture;
    }

    public CompletableFuture completableFuture() {
        if (status == Status.FAIL) {
            throw new IllegalArgumentException("Accessing completable future on a failed result");
        }

        return queuedFuture;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public DispatcherResult ifFailed(Runnable action) {
        if (!isSuccess()) {
            action.run();
        }

        return this;
    }
}
