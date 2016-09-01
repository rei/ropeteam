package com.rei.ropeteam;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.jgroups.JChannel;
import org.jgroups.util.Util;
import org.junit.Test;

public class JGroupsOncePerClusterExecutorTest {

    private static final String CLUSTER_NAME = "opc";

    @Test
    public void onlyExecutesOncePerCluster() throws Exception {
        final AtomicLong counter = new AtomicLong();
        
        CountDownLatch latch = new CountDownLatch(3);
        Action cmd = () -> counter.incrementAndGet();
        new Thread(createRunner(cmd, latch)).start();
        new Thread(createRunner(cmd, latch)).start();
        new Thread(createRunner(cmd, latch)).start();
        
        latch.await();
        
        assertEquals(2, counter.intValue());
    }
    
    private Runnable createRunner(final Action cmd, final CountDownLatch latch) throws Exception {
        final JChannel channel = new JChannel(Util.getTestStack());
        channel.connect(CLUSTER_NAME);
        
        return ()->{
                try {
                    OncePerClusterExecutor executor = new JGroupsOncePerClusterExecutor(channel);
                    executor.execute("mycmd", cmd);
                    executor.execute("mycmd", cmd);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                latch.countDown();
        };
    }

}
