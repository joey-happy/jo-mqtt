package joey.mqtt.broker.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static joey.mqtt.broker.Constants.*;

/**
 * topic 工具类
 *
 * @author Joey
 * @date 2019/8/29
 */
@Slf4j
public class TopicUtils {
    private TopicUtils() {

    }

    /**
     * topic不支持如下格式
     * <li>为空</li>
     * <li>以'/'开始或结束</li>
     * <li>命名为'joRootTopic'</li>
     * <li>分隔符之间无字符 例如：ab///c</li>
     * <li>包含禁用字符 例如：ab/+11 或者c/12#1</li>
     * @param topic
     * @return
     */
    public static List<String> getTokenList(String topic) {
        //topic为空 则不合法
        if (StrUtil.isBlank(topic)) {
            return CollUtil.newArrayList();
        }

        //topic以'/'开始或者结束 则不合法
        if (topic.startsWith(StrUtil.SLASH) || topic.endsWith(StrUtil.SLASH)) {
            return CollUtil.newArrayList();
        }

        //topic与root同名 或者 则不合法
        if (topic.startsWith(TOKEN_ROOT)) {
            return CollUtil.newArrayList();
        }

        //分隔后长度为0 则不合法
        String[] tokenArray = topic.split(StrUtil.SLASH);
        if (null == tokenArray || tokenArray.length == 0) {
            return CollUtil.newArrayList();
        }

        List<String> tokenList = CollUtil.newArrayList();
        for (int i = 0; i < tokenArray.length; i++) {
            String token = tokenArray[i];

            //token为空 则不合法
            if (StrUtil.isBlank(token)) {
                return CollUtil.newArrayList();
            }

            //如果'#'通配符不是最后一个字符 则不合法
            if (TOKEN_MULTI.equals(token) && i != tokenArray.length -1) {
                return CollUtil.newArrayList();
            }

            //如果不是'#'通配符 并且token中包含'#' 则不合法
            if (!TOKEN_MULTI.equals(token) && token.contains(TOKEN_MULTI)) {
                return CollUtil.newArrayList();
            }

            //如果不是'+'通配符 并且token中包含'+' 则不合法
            if (!TOKEN_SINGLE.equals(token) && token.contains(TOKEN_SINGLE)) {
                return CollUtil.newArrayList();
            }

            tokenList.add(token);
        }

        return tokenList;
    }

    /**
     * 消息匹配
     * @param subTokenList 订阅主题tokenList 可以包含通配符topic
     * @param matchTokenList 被匹配主题tokenList 无通配符topic
     * @return
     */
    public static boolean match(List<String> subTokenList, List<String> matchTokenList) {
        int i = 0;

        for (; i < subTokenList.size(); i++) {
            String subToken = subTokenList.get(i);

            if (!TOKEN_MULTI.equals(subToken) && !TOKEN_SINGLE.equals(subToken)) {
                if (i >= matchTokenList.size()) {
                    return false;
                }

                String matchToken = matchTokenList.get(i);
                if (!subToken.equals(matchToken)) {
                    return false;
                }
            } else {
                if (TOKEN_MULTI.equals(subToken)) {
                    return true;
                }

                if (TOKEN_SINGLE.equals(subToken)) {
                    //不做任何处理 继续遍历
                }
            }
        }

        return i == matchTokenList.size();
    }
}
