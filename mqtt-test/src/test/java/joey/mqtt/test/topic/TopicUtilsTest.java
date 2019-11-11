package joey.mqtt.test.topic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import joey.mqtt.broker.Constants;
import joey.mqtt.broker.util.TopicUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Joey
 * @date 2019/9/7
 */
public class TopicUtilsTest {

    @Test
    public void testGetTokenList() {
        String topic = "/";
        List<String> tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "abc/";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = Constants.TOKEN_ROOT;
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "+";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(tokenList.size() == 1 && topic.equals(tokenList.get(0)));

        topic = "+/";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "t1/++";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "#/11";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "+/";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "a/+";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertEquals(topic, StrUtil.join(StrUtil.SLASH, tokenList));

        topic = "a/c d";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertEquals(topic, StrUtil.join(StrUtil.SLASH, tokenList));

        topic = "a/+/#11";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "a/+/#";
        tokenList = TopicUtils.getTokenList(topic);
        Assert.assertEquals(topic, StrUtil.join(StrUtil.SLASH, tokenList));
    }

    @Test
    public void testMatch() {
        List<String> subTokenList = TopicUtils.getTokenList("a/+");
        List<String> matchTokenList = TopicUtils.getTokenList("a/b");
        boolean match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTokenList("a/b");
        matchTokenList = TopicUtils.getTokenList("a/+");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);

        subTokenList = TopicUtils.getTokenList("a/+/+");
        matchTokenList = TopicUtils.getTokenList("a/b");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);

        subTokenList = TopicUtils.getTokenList("a/+/+");
        matchTokenList = TopicUtils.getTokenList("a/b/+");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTokenList("a/#");
        matchTokenList = TopicUtils.getTokenList("a/b/+");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTokenList("a/b/+");
        matchTokenList = TopicUtils.getTokenList("a/#");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);

        subTokenList = TopicUtils.getTokenList("a/b/+/c/+");
        matchTokenList = TopicUtils.getTokenList("a/b/d/c/e");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTokenList("+/b");
        matchTokenList = TopicUtils.getTokenList("c/b");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTokenList("+/b");
        matchTokenList = TopicUtils.getTokenList("c/c");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);
    }
}
