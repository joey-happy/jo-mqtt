package joey.mqtt.test.topic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import joey.mqtt.broker.constant.BusinessConstants;
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
        List<String> tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "abc/";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = BusinessConstants.TOKEN_ROOT;
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "+";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(tokenList.size() == 1 && topic.equals(tokenList.get(0)));

        topic = "+/";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "t1/++";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "#/11";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "+/";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "a/+";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertEquals(topic, StrUtil.join(StrUtil.SLASH, tokenList));

        topic = "a/c d";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertEquals(topic, StrUtil.join(StrUtil.SLASH, tokenList));

        topic = "a/+/#11";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertTrue(CollUtil.isEmpty(tokenList));

        topic = "a/+/#";
        tokenList = TopicUtils.getTopicTokenList(topic);
        Assert.assertEquals(topic, StrUtil.join(StrUtil.SLASH, tokenList));
    }

    @Test
    public void testMatchTopic() {
        List<String> subTokenList = TopicUtils.getTopicTokenList("a/+");
        List<String> matchTokenList = TopicUtils.getTopicTokenList("a/b");
        boolean match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTopicTokenList("a/b");
        matchTokenList = TopicUtils.getTopicTokenList("a/+");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);

        subTokenList = TopicUtils.getTopicTokenList("a/+/+");
        matchTokenList = TopicUtils.getTopicTokenList("a/b");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);

        subTokenList = TopicUtils.getTopicTokenList("a/+/+");
        matchTokenList = TopicUtils.getTopicTokenList("a/b/+");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTopicTokenList("a/#");
        matchTokenList = TopicUtils.getTopicTokenList("a/b/+");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTopicTokenList("a/b/+");
        matchTokenList = TopicUtils.getTopicTokenList("a/#");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);

        subTokenList = TopicUtils.getTopicTokenList("a/b/+/c/+");
        matchTokenList = TopicUtils.getTopicTokenList("a/b/d/c/e");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTopicTokenList("+/b");
        matchTokenList = TopicUtils.getTopicTokenList("c/b");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertTrue(match);

        subTokenList = TopicUtils.getTopicTokenList("+/b");
        matchTokenList = TopicUtils.getTopicTokenList("c/c");
        match = TopicUtils.match(subTokenList, matchTokenList);
        Assert.assertFalse(match);
    }
}
