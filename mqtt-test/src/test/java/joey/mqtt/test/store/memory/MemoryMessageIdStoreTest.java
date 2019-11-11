package joey.mqtt.test.store.memory;

import cn.hutool.core.collection.ConcurrentHashSet;
import joey.mqtt.broker.store.memory.MemoryMessageIdStore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author Joey
 * @date 2019/9/8
 */
public class MemoryMessageIdStoreTest {
    @Test
    public void testNextId() {
        MemoryMessageIdStore store = new MemoryMessageIdStore();
        String clientId = "joey_test";

        for (int i = 0; i<100000; i++) {
            int nextMessageId = store.getNextMessageId(clientId);

            System.out.println(nextMessageId);

            if (nextMessageId <= 0) {
                Assert.fail(nextMessageId + "");
            }

            if (nextMessageId >= 65535) {
                Assert.fail(nextMessageId + "");
            }
        }
    }

    @Test
    public void testMultiThreadNextId() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(100);

        MemoryMessageIdStore store = new MemoryMessageIdStore();
        String clientId = "joey_test";

        Set<Integer> msgIdSet = new ConcurrentHashSet<>();

        /**
         * 此次测试只能并发取到msgId最大为60000 如果超过65534就会有重复id生成 是期望结果
         */
        for (int i=0; i<100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i=0; i<600; i++) {
                        int msgId = store.getNextMessageId(clientId);

                        //写入文件检测 cat tmp.txt| sort | uniq -c | sort -rn | wc -l
//                        FileUtil.appendUtf8String(msgId+"\n", "/tmp.txt");
                        System.out.println(msgId);

                        boolean add = msgIdSet.add(msgId);
                        if (!add) {
                            Assert.fail("duplicate msg id. msgId=" + msgId);
                        }
                    }

                    latch.countDown();
                }
            }).start();
        }

        latch.await();
    }
}
