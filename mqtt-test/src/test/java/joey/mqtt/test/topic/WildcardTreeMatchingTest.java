package joey.mqtt.test.topic;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.mqtt.MqttQoS;
import joey.mqtt.broker.constant.BusinessConstants;
import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.subscription.Subscription;
import joey.mqtt.broker.store.memory.MemorySubscriptionStore;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * @author Joey
 * @date 2019/8/29
 */
@Slf4j
public class WildcardTreeMatchingTest {
    private MemorySubscriptionStore subStore;

    @Before
    public void init() {
        subStore = new MemorySubscriptionStore(new CustomConfig());
    }

    @Test
    public void testTopicFormat() {
        String topic = "/";
        boolean addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = "abc/";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = BusinessConstants.TOKEN_ROOT;
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = "+";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertTrue(addSucc);

        topic = "+/";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = "t1/++";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = "#/11";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = "+/";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = "a/+";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertTrue(addSucc);

        topic = "a/c d";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertTrue(addSucc);

        topic = "a/+/#11";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertFalse(addSucc);

        topic = "a/+/#";
        addSucc = subStore.add(new Subscription(UUID.randomUUID().toString(), topic, MqttQoS.AT_LEAST_ONCE), false);
        Assert.assertTrue(addSucc);
    }

    @Test
    public void testMatchSimple() {
        log.info("Topic match start.");

        String subTopic = "t1/+";
        Subscription s1 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        boolean addSucc = subStore.add(s1, false);
        Assert.assertTrue(addSucc);

        subTopic = "t1/+";
        Subscription s2 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s2, false);
        Assert.assertTrue(addSucc);

        subTopic = "t1/+/a";
        Subscription s3 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s3, false);
        Assert.assertTrue(addSucc);

        subTopic = "t1/+/+";
        Subscription s4 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s4, false);
        Assert.assertTrue(addSucc);

        subTopic = "t1/#";
        Subscription s5 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s5, false);
        Assert.assertTrue(addSucc);

        //================================
        subTopic = "t2";
        Subscription s6 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s6, false);
        Assert.assertTrue(addSucc);

        subTopic = "t2/abc";
        Subscription s7 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s7, false);
        Assert.assertTrue(addSucc);

        subTopic = "t2/ABC";
        Subscription s8 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s8, false);
        Assert.assertTrue(addSucc);

        subTopic = "t2/ABC/#";
        Subscription s9 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s9, false);
        Assert.assertTrue(addSucc);

        subTopic = "t2/ABC/+";
        Subscription s10 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s10, false);
        Assert.assertTrue(addSucc);

        subTopic = "t2/ABC/+/D/+";
        Subscription s11 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s11, false);
        Assert.assertTrue(addSucc);

        subTopic = "t2/#";
        Subscription s12 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s12, false);
        Assert.assertTrue(addSucc);

        //========================
        subTopic = "+/ABC/#";
        Subscription s13 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s13, false);
        Assert.assertTrue(addSucc);

        subTopic = "+";
        Subscription s14 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s14, false);
        Assert.assertTrue(addSucc);

        subTopic = "+/+";
        Subscription s15 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s15, false);
        Assert.assertTrue(addSucc);

        subTopic = "+/b/+";
        Subscription s16 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s16, false);
        Assert.assertTrue(addSucc);

        subTopic = "#";
        Subscription s0 = new Subscription(UUID.randomUUID().toString(), subTopic, null);
        addSucc = subStore.add(s0, false);
        Assert.assertTrue(addSucc);

        //=====================================================
        String matchTopic = "t1/a";
        List<Subscription> matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s1, s2, s5, s15), CollUtil.newHashSet(matchSubList));

        matchTopic = "t1/b/a";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s3, s4, s5, s16), CollUtil.newHashSet(matchSubList));

        matchTopic = "t1/b/c";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s4, s5, s16), CollUtil.newHashSet(matchSubList));

        matchTopic = "t1/b/c/d";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s5), CollUtil.newHashSet(matchSubList));

        //================================================================
        matchTopic = "t2";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s6, s14), CollUtil.newHashSet(matchSubList));

        matchTopic = "t2/abc";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s7, s12, s15), CollUtil.newHashSet(matchSubList));

        matchTopic = "t2/abc/dd";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s12), CollUtil.newHashSet(matchSubList));

        matchTopic = "t2/ABC";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s8, s12, s15), CollUtil.newHashSet(matchSubList));

        matchTopic = "t2/ABC/aaa";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s9, s10, s12, s13), CollUtil.newHashSet(matchSubList));

        matchTopic = "t2/ABC/aaa/D";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s9, s12, s13), CollUtil.newHashSet(matchSubList));

        matchTopic = "t2/ABC/BBB/D/CC";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s9, s11, s12, s13), CollUtil.newHashSet(matchSubList));

        matchTopic = "t3/A";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s0, s15), CollUtil.newHashSet(matchSubList));

        Console.log();
        Console.log("===================================");
        Console.log(subStore.dumpWildcardSubData());

        //====================================================
        subStore.remove(s0);
        matchTopic = "t1/a";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s1, s2, s5, s15), CollUtil.newHashSet(matchSubList));

        subStore.remove(s3);
        matchTopic = "t1/b/a";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s4, s5, s16), CollUtil.newHashSet(matchSubList));

        subStore.remove(s5);
        matchTopic = "t1/b/c/d";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(), CollUtil.newHashSet(matchSubList));

        subStore.remove(s1);
        subStore.remove(s2);
        subStore.remove(s4);
        //=======================================

        subStore.remove(s8);
        matchTopic = "t2/ABC";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s12, s15), CollUtil.newHashSet(matchSubList));

        subStore.remove(s10);
        matchTopic = "t2/ABC/aaa";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s9, s12, s13), CollUtil.newHashSet(matchSubList));

        subStore.remove(s12);
        subStore.remove(s13);
        matchTopic = "t2/ABC/BBB/D/CC";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(s9, s11), CollUtil.newHashSet(matchSubList));

        subStore.remove(s9);
        matchTopic = "t2/ABC/aaa/D";
        matchSubList = subStore.match(matchTopic);
        Console.log("topic:" +  matchTopic + ",matchSubSet:" + JSON.toJSONString(matchSubList));
        Assert.assertEquals(CollUtil.newHashSet(), CollUtil.newHashSet(matchSubList));

        Console.log();
        Console.log("===================================");
        Console.log(subStore.dumpWildcardSubData());
    }
}
