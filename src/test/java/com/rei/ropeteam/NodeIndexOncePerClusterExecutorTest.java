package com.rei.ropeteam;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

public class NodeIndexOncePerClusterExecutorTest {
    @Test
    public void execute() throws Exception {
        final AtomicLong counter = new AtomicLong();

        CountDownLatch latch = new CountDownLatch(3);
        Action cmd = counter::incrementAndGet;
        new Thread(createRunner(cmd, latch, 0)).start();
        new Thread(createRunner(cmd, latch, 1)).start();
        new Thread(createRunner(cmd, latch, 2)).start();

        latch.await();

        assertEquals(2, counter.intValue());
    }

    private Runnable createRunner(Action cmd, CountDownLatch latch, int nodeIndex) {
        return ()->{
            try {
                OncePerClusterExecutor executor = new NodeIndexOncePerClusterExecutor(String.valueOf(nodeIndex));
                executor.execute("mycmd", cmd);
                executor.execute("mycmd", cmd);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            latch.countDown();
        };
    }

}