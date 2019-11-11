package joey.mqtt.broker.event.message;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ping事件消息
 *
 * @author Joey
 * @date 2019/9/8
 */
@AllArgsConstructor
@Getter
public class PingEventMessage implements EventMessage {
    private final String clientId;

    private final String userName;

    @Override
    public String info() {
        return JSONObject.toJSONString(this);
    }
}
