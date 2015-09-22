package com.rei.ropeteam.spring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import com.rei.ropeteam.ClusterEventBus;

public class EventSubscriberRegistrar implements ApplicationContextAware {
    
    private static final Logger logger = LoggerFactory.getLogger(EventSubscriberRegistrar.class);

    private ApplicationContext applicationContext;
    private ClusterEventBus eventBus;

    public EventSubscriberRegistrar(ClusterEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        
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
            .collect(Collectors.toList());
        
       Predicate<Method> filter = m -> m.getReturnType().equals(Void.TYPE) && m.getParameterCount() == 1;
       candidateMethods.stream().filter(filter).forEach(this::registerSubscriberMethod);
       
       candidateMethods.stream().filter(filter.negate()).forEach(m -> 
           logger.warn("skipping event subscriber method {}, because it returns a value or accepts more than one parameter", m));
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
