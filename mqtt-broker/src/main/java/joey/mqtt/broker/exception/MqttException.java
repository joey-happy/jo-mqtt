package joey.mqtt.broker.exception;

/**
 * 异常
 * @author Joey
 * @date 2019/7/23
 */
public class MqttException extends RuntimeException {
    public MqttException() {
        super();
    }

    public MqttException(String message) {
        super(message);
    }

    public MqttException(String message, Throwable cause) {
        super(message, cause);
    }

    public MqttException(Throwable cause) {
        super(cause);
    }

    protected MqttException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
