flock
=====

distributed events and coordination for jgroups clusters in spring applications.

[![Build Status](https://travis-ci.org/jeffskj/flock.svg?branch=master)](https://travis-ci.org/jeffskj/flock)

Features
--------
  * Cluster member task limiting
  * Clustered Event Bus
  
Task Limiting
-------------

Limit a task or method call to one cluster member. This is useful for scheduled tasks that you 
only want running on one member. Flock allows dynamic assignment of which cluster member runs a task. 
A task will stay assigned to the same member unless it dies, in which case it will failover to another host.  

**Example**

    @OncePerCluster
    @Scheduled(fixedDelay=1000)
    public void someScheduledTask() {
      // method will only actually be called on one host per cluster
      // the same host will always run the task unless it dies 
    }

Clustered Event Bus
-------------------

Publish events to all members of the cluster. Events can be any object that can be marshalled to JSON with Jackson.

**Publishing Events**
    
    @Autowired
    EventPublisher publisher;
    
    public void generateEvent() {
       publisher.publish(new HelloEvent("greetings"));
    }
    
**Subscribing to Events**

    @EventSubscriber
    public void handleHelloEvent(HelloEvent e) {
       System.out.println(e.getGreeting())
    }
