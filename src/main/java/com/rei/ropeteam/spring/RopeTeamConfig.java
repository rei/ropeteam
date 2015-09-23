package com.rei.ropeteam.spring;

import org.jgroups.JChannel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.rei.ropeteam.ClusterEventBus;
import com.rei.ropeteam.OncePerClusterExecutor;

@Configuration
public class RopeTeamConfig {
    
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public static OncePerClusterExecutor oncePerClusterExecutor(JChannel mainChannel) {
        return new OncePerClusterExecutor(mainChannel);
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public static OnePerClusterInterceptor onePerClusterInterceptor(OncePerClusterExecutor executor) {
        return new OnePerClusterInterceptor(executor);
    }
    
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public static ClusterEventBus eventBus(JChannel mainChannel) {
        return new ClusterEventBus(mainChannel);
    }
    
    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public static EventSubscriberRegistrar eventSubscriberRegistrar(ClusterEventBus bus) {
        return new EventSubscriberRegistrar(bus);
    }
}
