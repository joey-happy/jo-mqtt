package joey.mqtt.broker.event.message;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 取消订阅事件消息
 *
 * @author Joey
 * @date 2019/9/8
 */
@Getter
@AllArgsConstructor
public class UnsubscribeEventMessage implements EventMessage {
    private final String topic;

    private final String clientId;

    private final String userName;

    @Override
    public String info() {
        return JSONObject.toJSONString(this);
    }
}
