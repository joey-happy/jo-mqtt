package joey.mqtt.broker.auth;

import joey.mqtt.broker.enums.AuthTopicOperationEnum;

/**
 * 连接授权
 *
 * @author Joey
 * @date 2022/7/22
 */
public interface IAuth {
    /**
     * 检查用户名和密码
     *
     * @param userName
     * @param password
     * @return
     */
    boolean checkUserAuth(String userName, byte[] password);

    /**
     * 检查topic权限
     *
     * @param clientId
     * @param topic
     * @param topicOperationEnum
     * @return
     */
    boolean checkTopicAuth(String clientId, String topic, AuthTopicOperationEnum topicOperationEnum);
}
