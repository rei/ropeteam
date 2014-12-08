package io.github.flock;

import java.io.IOException;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.fork.ForkChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ClusterEventBus implements EventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ClusterEventBus.class);
    
    private Multimap<Class, EventSubscriber> subscribers = ArrayListMultimap.create();
    
    private ForkChannel channel;
    
    private ObjectMapper mapper = new ObjectMapper();

    public ClusterEventBus(JChannel main) {
        channel = Forks.fork(main, "eventbus");
        channel.receiver(new EventBusReceiver());
    }

    public void subscribe(Class<?> eventType, EventSubscriber listener) {
        subscribers.put(eventType, listener);
    }
    
    @Override
    public void publish(Object event) {
        try {
            channel.send(null, toBytes(event));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("unable to write event as json", e);
        } catch (Exception e) {
            throw new RuntimeException("failed to send event", e);
        }
    }

    private byte[] toBytes(Object event) throws JsonProcessingException {
        String s = mapper.writer().writeValueAsString(event);
        byte[] body = (event.getClass().getName() + "\n" + s).getBytes();
        return body;
    }
    
    private Object fromBytes(byte[] bytes) throws ClassNotFoundException, IOException {
        String s = new String(bytes);
        String className = s.substring(0, s.indexOf('\n'));
        Class<?> eventType = Class.forName(className);
        ObjectReader reader = mapper.reader(eventType);
        String payload = s.substring(className.length());
        return reader.readValue(payload);
    }
    
    private void receiveEvent(Object event) {
        for (EventSubscriber listener : subscribers.get(event.getClass())) {
            listener.receiveEvent(event);
        }
    }
    
    public class EventBusReceiver extends ReceiverAdapter {
        @Override
        public void receive(Message msg) {
            try {
                receiveEvent(fromBytes(msg.getBuffer()));
            } catch (ClassNotFoundException e) {
                logger.warn("received event for unknown type", e);
            } catch (IOException e) {
                logger.warn("failed to receive event", e);
            }
        }
    }
}