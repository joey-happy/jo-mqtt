package joey.mqtt.broker.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author：Joey
 * @Date: 2022/7/23
 * @Desc: 服务协议类型枚举
 **/
@AllArgsConstructor
@Getter
public enum ServerProtocolTypeEnum {
    TCP("tcp"),

    WEB_SOCKET("webSocket"),
    ;

    public final String name;
}
