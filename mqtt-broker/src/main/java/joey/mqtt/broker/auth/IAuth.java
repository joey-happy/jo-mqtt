package joey.mqtt.broker.auth;

/**
 * 连接授权
 * @author Joey
 * @date 2019/7/22
 */
public interface IAuth {
    /**
     * 检查用户名和密码
     *
     * @param userName
     * @param password
     * @return
     */
    boolean checkValid(String userName, byte[] password);
}
