package joey.mqtt.broker.event.message;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * pubAck事件消息
 *
 * @author Joey
 * @date 2019/9/17
 */
@AllArgsConstructor
@Getter
public class PubAckEventMessage implements EventMessage {
    private final String clientId;

    private final String userName;

    private final Integer messageId;

    @Override
    public String info() {
        return JSONObject.toJSONString(this);
    }
}
