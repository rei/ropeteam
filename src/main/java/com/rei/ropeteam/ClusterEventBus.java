package com.rei.ropeteam;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.fork.ForkChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ClusterEventBus implements EventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ClusterEventBus.class);
    
    private Map<Class<?>, List<EventSubscriber>> subscribers = new HashMap<>();
    
    private ForkChannel channel;
    
    private ObjectMapper mapper = new ObjectMapper();
    {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ClusterEventBus(JChannel main) {
        channel = Forks.fork(main, "eventbus");
        channel.receiver(new EventBusReceiver());
    }

    public void subscribe(Class<?> eventType, EventSubscriber listener) {
        subscribers.compute(eventType, (k, v) -> v == null ? new LinkedList<>() : v).add(listener);
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