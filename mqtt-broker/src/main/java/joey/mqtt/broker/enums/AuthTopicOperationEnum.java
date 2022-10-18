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
public enum AuthTopicOperationEnum {
    READ, WRITE, READ_WRITE
}
