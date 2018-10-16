package com.rei.ropeteam.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.rei.ropeteam.EventPublisher;
import com.rei.ropeteam.spring.EventSubscriber;
import com.rei.ropeteam.spring.OnePerCluster;

public class TestBean {
    private int opcInvocations = 0;
    private List<Event> receivedEvents = new ArrayList<>();
    private List<BlankEvent> blankEvents = new ArrayList<>();
    
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
    public void blankSubscriber(BlankEvent e) {
        System.out.println("blank event");
        blankEvents.add(e);
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
    
    public List<BlankEvent> getBlankEvents() {
        return blankEvents;
    }
}
