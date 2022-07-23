package joey.mqtt.broker.auth.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import joey.mqtt.broker.auth.AuthUser;
import joey.mqtt.broker.auth.IAuth;
import joey.mqtt.broker.config.CustomConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认授权实现
 * @author Joey
 * @date 2019/7/22
 */
public class DefaultAuthImpl implements IAuth {
    private final Map<String, String> userPassMap = new HashMap<>();

    private final CustomConfig customConfig;

    public DefaultAuthImpl(List<AuthUser> userList, CustomConfig customConfig) {
        if (CollUtil.isNotEmpty(userList)) {
            userList.forEach(user -> {
                userPassMap.put(user.getUserName(), user.getPassword());
            });
        }

        this.customConfig = customConfig;
    }

    @Override
    public boolean checkAuth(String userName, byte[] password) {
        if (StrUtil.isBlank(userName) || null == password) {
            return false;
        }

        String authPass = userPassMap.get(userName);
        if (StrUtil.isBlank(authPass)) {
            return false;
        }

        String encodedPass = SecureUtil.sha256(new String(password));
        if (ObjectUtil.notEqual(authPass, encodedPass)) {
            return false;
        }

        return true;
    }
}
