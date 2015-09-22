package com.rei.ropeteam;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.jgroups.JChannel;
import org.jgroups.util.Util;
import org.junit.Test;

import com.rei.ropeteam.OncePerClusterExecutor;

public class OncePerClusterExecutorTest {

    private static final String CLUSTER_NAME = "opc";

    @Test
    public void onlyExecutesOncePerCluster() throws Exception {
        final AtomicLong counter = new AtomicLong();
        
        Runnable cmd = new Runnable() {
            @Override
            public void run() {
                counter.incrementAndGet();
            }
        };
        
        CountDownLatch latch = new CountDownLatch(3);
        
        new Thread(createRunner(cmd, latch)).start();
        new Thread(createRunner(cmd, latch)).start();
        new Thread(createRunner(cmd, latch)).start();
        
        latch.await();
        
        assertEquals(2, counter.intValue());
    }
    
    private Runnable createRunner(final Runnable cmd, final CountDownLatch latch) throws Exception {
        final JChannel channel = new JChannel(Util.getTestStack());
        channel.connect(CLUSTER_NAME);
        
        return new Runnable() {
            @Override
            public void run() {
                try {
                    OncePerClusterExecutor executor = new OncePerClusterExecutor(channel);
                    executor.execute("mycmd", cmd);
                    executor.execute("mycmd", cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        };
    }

}
