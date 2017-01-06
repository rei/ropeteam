package com.rei.ropeteam.cluster;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cluster {
    private static final Logger logger = LoggerFactory.getLogger(Cluster.class);

    public static final String DEFAULT_TYPE = "default";

    private final ScheduledExecutorService discoveryScheduler;
    private final ForkJoinPool workerPool;
    private final ForkJoinPool discoverPool;
    private Transport transport;
    private MemberDiscovery discovery;
    private Set<Member> currentView = new ConcurrentHashMap<Member, Object>().keySet(new Object());
    private Set<Member> potentialMembers = new ConcurrentHashMap<Member, Object>().keySet(new Object());
    private ConcurrentMap<String, Consumer<Message>> handlers = new ConcurrentHashMap<>();

    private Cluster(Transport transport, MemberDiscovery discovery, int workerPoolSize, int discoverPoolSize) {
        this.transport = transport;
        this.discovery = discovery;
        this.workerPool = new ForkJoinPool(workerPoolSize);
        this.discoverPool = new ForkJoinPool(discoverPoolSize);
        discoveryScheduler = Executors.newScheduledThreadPool(2);
    }

    public void start() {
        setDefaultHandler(m -> logger.debug("received message on default handler"));

        discoveryScheduler.scheduleWithFixedDelay(() -> {
            logger.info("running discovery");
            potentialMembers.addAll(discovery.findPotentialMembers());

            discoverPool.submit(() -> {
                potentialMembers.parallelStream().forEach(member -> {
                    potentialMembers.addAll(transport.getPeers(member));
                });
            });
            logger.info("discovery found potential members: {}", potentialMembers);
        }, 0, 1, TimeUnit.MINUTES);

        discoveryScheduler.scheduleWithFixedDelay(() -> {
            discoverPool.submit(() -> {
                Set<Member> healthyMembers = potentialMembers.parallelStream()
                        .filter(transport::healthCheck)
                        .collect(toSet());
                logger.info("adding healthy nodes {} to view", healthyMembers);
                currentView.addAll(healthyMembers);
            });
        }, 1, 10, TimeUnit.SECONDS);
    }

    public void sendMessage(Message message) {
        workerPool.submit(() -> {
            currentView.parallelStream().forEach(member -> transport.send(message, member));
        });
    }

    public void messageReceived(Message message) {
        workerPool.submit(() -> getHandler(message.getType()).accept(message));
    }

    public Cluster setDefaultHandler(Consumer<Message> handler) {
        return setHandler(DEFAULT_TYPE, handler);
    }

    public Cluster setHandler(String type, Consumer<Message> handler) {
        handlers.put(type, handler);
        return this;
    }

    public Set<Member> getCurrentView() {
        return Collections.unmodifiableSet(currentView);
    }

    private Consumer<Message> getHandler(String type) {
        return handlers.getOrDefault(type, handlers.get(DEFAULT_TYPE));
    }

    public static class Builder {
        private Transport transport;
        private MemberDiscovery discovery;
        private int workerPoolSize = 10;
        private int discoverPoolSize = 10;

        public Builder transport(Transport t) {
            this.transport = t;
            return this;
        }

        public Builder discovery(MemberDiscovery discovery) {
            this.discovery = discovery;
            return this;
        }

        public Builder workerPoolSize(int size) {
            this.workerPoolSize = size;
            return this;
        }

        public Builder discoverPoolSize(int size) {
            this.discoverPoolSize = size;
            return this;
        }

        public Cluster build() {
            return new Cluster(transport, discovery, workerPoolSize, discoverPoolSize);
        }
    }
}
