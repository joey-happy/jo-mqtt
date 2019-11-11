package joey.mqtt.broker.event.message;

import com.alibaba.fastjson.JSONObject;
import com.hazelcast.internal.json.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 连接丢失事件消息
 *
 * @author Joey
 * @date 2019/9/8
 */
@Getter
@AllArgsConstructor
public class ConnectionLostEventMessage implements EventMessage {
    private final String clientId;

    private final String userName;

    @Override
    public String info() {
        return JSONObject.toJSONString(this);
    }
}
