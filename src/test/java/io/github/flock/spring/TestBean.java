package io.github.flock.spring;

import io.github.flock.EventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

public class TestBean {
    private int opcInvocations = 0;
    private List<Event> receivedEvents = new ArrayList<>();
    
    @Autowired
    private EventPublisher publisher;
    
    @OnePerCluster
    public void oncePerCluster() {
        opcInvocations++;
    }
    
    public void publishEvent() {
        Event event = new Event();
        event.setData(UUID.randomUUID().toString());
        publisher.publish(event);
    }
    
    @EventSubscriber
    public Event badSubscriber(Event e) {
        System.out.println("BAD: " + e.getData());
        receivedEvents.add(e);
        return e;
    }
    
    @EventSubscriber
    public void receiveEvent(Event e) {
        System.out.println("Received event: " + e.getData());
        receivedEvents.add(e);
    }
    
    public int getOpcInvocations() {
        return opcInvocations;
    }
    
    public List<Event> getReceivedEvents() {
        return receivedEvents;
    }
}
