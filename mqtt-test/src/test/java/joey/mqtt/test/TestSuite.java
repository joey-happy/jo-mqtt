package joey.mqtt.test;

import joey.mqtt.test.store.memory.MemoryMessageIdStoreTest;
import joey.mqtt.test.topic.TopicUtilsTest;
import joey.mqtt.test.wildcardtree.WildcardTreeMatchingTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Joey
 * @date 2019/9/16
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TopicUtilsTest.class,
        MemoryMessageIdStoreTest.class,
        WildcardTreeMatchingTest.class
})
public class TestSuite {
}
