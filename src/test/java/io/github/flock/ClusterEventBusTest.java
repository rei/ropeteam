package io.github.flock;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.jgroups.JChannel;
import org.jgroups.util.Util;
import org.junit.Test;

public class ClusterEventBusTest {
    private static final String CLUSTER_NAME = "eventbus";
    
    @Test
    public void canPublishAndReceiveEvents() throws Exception {
        Collection<Object> received = new ConcurrentLinkedDeque<Object>(); 
        ClusterEventBus mainBus = createBus(received);
        
        createBus(received);
        createBus(received);
        
        mainBus.publish(new MyEvent(123, "this is a test"));
        
        Thread.sleep(100);
        
        assertEquals(3, received.size());
        received.clear();
        
        mainBus.publish(new MyEvent(456, "another test"));
        
        Thread.sleep(100);
        
        assertEquals(3, received.size());
    }

    private ClusterEventBus createBus(final Collection<Object> received) throws Exception {
        final JChannel channel = new JChannel(Util.getTestStack());
        channel.connect(CLUSTER_NAME);
        
        ClusterEventBus bus = new ClusterEventBus(channel);
        bus.subscribe(MyEvent.class, new EventSubscriber() {
            
            @Override
            public void receiveEvent(Object event) {
                MyEvent msg = (MyEvent) event;
                System.out.println(msg.getMsg());
                received.add(msg);
            }
        });
        
        return bus;
    }

    public static class MyEvent {
        private int id;
        private String msg;
        
        public MyEvent() {
        }
        
        public MyEvent(int id, String msg) {
            this.id = id;
            this.msg = msg;
        }

        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getMsg() {
            return msg;
        }
        
        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
