package joey.mqtt.broker.core.dispatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @Author：Joey
 * @Date: 2022/7/25
 * @Desc: 分发命令
 **/
public class DispatcherCommand {
    private final String clientId;

    private final Callable<String> action;

    private final CompletableFuture<String> task;

    public DispatcherCommand(String clientId, Callable<String> action) {
        this.clientId = clientId;
        this.action = action;
        this.task = new CompletableFuture<>();
    }

    public void execute() throws Exception {
        action.call();
    }

    public void complete() {
        task.complete(clientId);
    }

    public CompletableFuture<String> completableFuture() {
        return task;
    }

    public String getClientId() {
        return this.clientId;
    }
}
