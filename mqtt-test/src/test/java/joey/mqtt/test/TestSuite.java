package joey.mqtt.test;

import joey.mqtt.test.pubSub.RetainMessageTest;
import joey.mqtt.test.topic.TopicUtilsTest;
import joey.mqtt.test.topic.WildcardTreeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 测试套件
 *
 * @author Joey
 * @date 2019/9/16
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TopicUtilsTest.class,
        WildcardTreeTest.class,
        RetainMessageTest.class
})
public class TestSuite {
}
