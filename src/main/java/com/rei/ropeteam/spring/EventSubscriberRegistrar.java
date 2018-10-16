package com.rei.ropeteam.spring;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.rei.ropeteam.ClusterEventBus;

public class EventSubscriberRegistrar {
    
    private static final Logger logger = LoggerFactory.getLogger(EventSubscriberRegistrar.class);

    private ApplicationContext applicationContext;
    private ClusterEventBus eventBus;

    public EventSubscriberRegistrar(ClusterEventBus eventBus, ApplicationContext ctx) {
        this.eventBus = eventBus;
        this.applicationContext = ctx;
    }

    @PostConstruct
    public void registerListeners() {
        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext)applicationContext).getBeanFactory();
        
        Collection<Method> candidateMethods = Arrays.stream(applicationContext.getBeanDefinitionNames())
            .filter(n -> beanFactory.getBeanDefinition(n).isPrototype() || beanFactory.getBeanDefinition(n).isSingleton())
            .map(n -> applicationContext.getType(n).getMethods())
            .flatMap(Arrays::stream)
            .filter(m -> m.isAnnotationPresent(EventSubscriber.class))
            .distinct()
            .collect(toList());
        
       Predicate<Method> filter = m -> m.getReturnType().equals(Void.TYPE) && m.getParameterCount() == 1;

        List<Method> invalidMethods = candidateMethods.stream().filter(filter.negate()).collect(toList());
        if (!invalidMethods.isEmpty()) {
            String methodList = invalidMethods.stream().map(Objects::toString).collect(Collectors.joining(","));

            throw new IllegalArgumentException("invalid event subscriber methods " + methodList
                                                       + ". Subscriber methods must return 'void' and accept only 1 parameter!");
        }

        candidateMethods.stream()
                        .filter(filter)
                        .forEach(this::registerSubscriberMethod);
    }
    
    private void registerSubscriberMethod(Method m) {
        logger.info("registering subscription method {}", m);
        eventBus.subscribe(m.getParameters()[0].getType(), (Object event) -> { 
            try {
                System.out.println("sending event: " + event);
                m.invoke(applicationContext.getBean(m.getDeclaringClass()), event);
            } catch (Exception e) {
                logger.error("error processing event", e);
            } 
        });
    }
}
