package com.rei.ropeteam.cluster;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ClusterTest {
    private Cluster cluster;

    @Test
    public void canDiscoverPeers() throws InterruptedException {
        cluster = new Cluster.Builder()
                .discovery(() -> ImmutableSet.of(new Member("a", 1), new Member("b", 1), new Member("c", 1)))
                .transport(new MockTransport())
                .build();

        cluster.start();

        for (int i = 0; i < 10; i++) {
            Thread.sleep(200);
            System.out.println(cluster.getCurrentView());
        }
    }

    private class MockTransport implements Transport {
        @Override
        public void send(Message message, Member member) {
            cluster.messageReceived(message);
        }

        @Override
        public List<Member> getPeers(Member member) {
            return Arrays.asList(member);
        }

        @Override
        public boolean healthCheck(Member member) {
            return true;
        }
    }
}