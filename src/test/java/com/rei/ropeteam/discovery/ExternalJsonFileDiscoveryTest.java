package com.rei.ropeteam.discovery;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import fi.iki.elonen.NanoHTTPD;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.protocols.FRAG2;
import org.jgroups.protocols.UNICAST3;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.stack.IpAddress;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalJsonFileDiscoveryTest {
    private CountDownLatch countDownLatch = new CountDownLatch(9);

    @Test
    public void findMembers() throws Exception {

        ClusterMemberService.ClusterMember a = createMember("a");
        ClusterMemberService.ClusterMember b = createMember("b");
        ClusterMemberService.ClusterMember c = createMember("c");

        ClusterMemberService.ClusterMembers members = new ClusterMemberService.ClusterMembers();
        members.setMembers(Arrays.asList(a, b, c));

        JsonServer server = new JsonServer(members);
        server.start();

        List<JChannel> channels = members.getMembers().parallelStream()
                .map(m -> createChannel(server.getListeningPort(), m.getPort(), m.getHost()))
                .collect(toList());

        channels.forEach(ch -> {
            System.out.println(ch.getAddressAsString() + " View:" + ch.getViewAsString());
            assertEquals("a", ((UniqueStringAddress) ch.getView().getCoord()).getUniqueString());
            assertEquals(3, ch.getView().getMembers().size());
            try {
                ch.send(null, "test message");
                Thread.sleep(50);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        boolean success = countDownLatch.await(1, TimeUnit.SECONDS);
        assertTrue("only received " + countDownLatch.getCount() + " messages!", success);
    }

    private JChannel createChannel(int serverPort, int port, String host) {
        try {

            ClusterMemberService service = new ClusterMemberService("http://127.0.0.1:" + serverPort);
            service.setAddressLookup(this::mockAddressLookup);
            service.setHostnameSupplier(() -> host);

            JChannel channel = new JChannel(new DynamicPortTcpNio2(service)
                                                    .setBindAddress(InetAddress.getLoopbackAddress())
                                                    .setBindPort(port)
                                                    .setPortRange(1),
                                            new ExternalJsonFileDiscovery(service),
                                            new NAKACK2(),
                                            new UNICAST3(),
                                            new STABLE(),
                                            new GMS().joinTimeout(1000),
                                            new FRAG2().fragSize(8000));

            channel.addAddressGenerator(() -> getLocalIpAddress(service.getHostname(), port));
            channel.receiver(msg -> countDownLatch.countDown());
            channel.connect("test-cluster");
            return channel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UniqueStringAddress mockAddressLookup(ClusterMemberService.ClusterMember member) {
        return getLocalIpAddress(member.getId(), member.getPort());
    }

    private UniqueStringAddress getLocalIpAddress(String id, int port) {
        try {
            return new UniqueStringAddress(id, new IpAddress("127.0.0.1", port));
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private ClusterMemberService.ClusterMember createMember(String host) throws IOException {
        ClusterMemberService.ClusterMember member = new ClusterMemberService.ClusterMember();
        member.setHost(host);
        member.setId(host);
        member.setPort(findOpenPort());
        return member;
    }

    private int findOpenPort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    private static class JsonServer extends NanoHTTPD {
        private String stringContent;

        public JsonServer(Object content) throws JsonProcessingException {
            super(0);
            this.stringContent = new ObjectMapper().writeValueAsString(content);
        }

        @Override
        public Response serve(IHTTPSession session) {
            return newFixedLengthResponse(stringContent);
        }
    }
}