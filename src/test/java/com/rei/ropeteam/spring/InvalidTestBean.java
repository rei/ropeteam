package com.rei.ropeteam.spring;

public class InvalidTestBean {
    @EventSubscriber
    public Event badSubscriber(Event e) {
        System.out.println("BAD: " + e.getData());
        return e;
    }
}
