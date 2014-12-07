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

public class ClusterEventBus {
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
    
    public void publishEvent(Object event) {
        try {
            String s = mapper.writer().writeValueAsString(event);
            byte[] body = (event.getClass().getName() + "\n" + s).getBytes();
            channel.send(new Message(null, body));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("unable to write event as json", e);
        } catch (Exception e) {
            throw new RuntimeException("failed to send event", e);
        }
    }
    
    private void receiveEvent(Object event) {
        for (EventSubscriber listener : subscribers.get(event.getClass())) {
            listener.receiveEvent(event);
        }
    }
    
    public class EventBusReceiver extends ReceiverAdapter {
        @Override
        public void receive(Message msg) {
            String s = new String(msg.getBuffer());
            String className = s.substring(0, s.indexOf('\n'));
            try {
                Class<?> eventType = Class.forName(className);
                ObjectReader reader = mapper.reader(eventType);
                String payload = s.substring(className.length());
                receiveEvent(reader.readValue(payload));
            } catch (ClassNotFoundException e) {
                logger.warn("received event for unknown type: {}", className);
            } catch (IOException e) {
                logger.warn("failed to receive event of type: {}", className, e);
            }
        }
    }
}