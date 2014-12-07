package io.github.flock;

public interface EventSubscriber {
    void receiveEvent(Object event);
}
