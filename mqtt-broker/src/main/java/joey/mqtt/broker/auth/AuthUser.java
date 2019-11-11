package joey.mqtt.broker.auth;

import lombok.Getter;
import lombok.Setter;

/**
 * 授权用户
 *
 * @author Joey
 * @date 2019/9/7
 */
@Setter
@Getter
public class AuthUser {
    private String userName;

    private String password;
}
