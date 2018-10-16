package com.rei.ropeteam.spring;

import static org.junit.Assert.assertEquals;

import org.jgroups.JChannel;
import org.jgroups.logging.LogFactory;
import org.jgroups.util.Util;
import org.junit.Test;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.rei.ropeteam.EventPublisher;
import com.rei.ropeteam.JgroupsLogFactoryAdapter;

public class SpringIntegrationTest {
    static {
        LogFactory.setCustomLogFactory(new JgroupsLogFactoryAdapter());
    }

    @Test(expected = BeanCreationException.class)
    @SuppressWarnings("resource")
    public void blowsUpContextIfInvalidSubscriber() {
        new AnnotationConfigApplicationContext(InvalidTestSpringConfig.class);
    }

    @Test(expected = BeanCreationException.class)
    @SuppressWarnings("resource")
    public void blowsUpContextIfInvalidOnePerCluster() {
        new AnnotationConfigApplicationContext(InvalidOpcTestSpringConfig.class);
    }

    @Test
    @SuppressWarnings("resource")
    public void interceptsOncePerClusterAnnotatedStuff() throws InterruptedException {
        ApplicationContext context1 = new AnnotationConfigApplicationContext(ValidTestSpringConfig.class);
        ApplicationContext context2 = new AnnotationConfigApplicationContext(ValidTestSpringConfig.class);
        context1.getBean(TestBean.class).publishEvent();
        context1.getBean(TestBean.class).oncePerCluster();
        context2.getBean(TestBean.class).oncePerCluster();
        
        context2.getBean(EventPublisher.class).publish(new BlankEvent());
        context2.getBean(EventPublisher.class).publish(new BlankEvent());
        
        Thread.sleep(100);
        
        assertEquals(1, context2.getBean(TestBean.class).getReceivedEvents().size());
        
        assertEquals(1, context1.getBean(TestBean.class).getOpcInvocations());
        assertEquals(0, context2.getBean(TestBean.class).getOpcInvocations());
        
        assertEquals(2, context1.getBean(TestBean.class).getBlankEvents().size());
        assertEquals(2, context2.getBean(TestBean.class).getBlankEvents().size());
    }

    @Configuration
    @Import(RopeTeamConfig.class)
    public static class BaseTestSpringConfig {
        @Bean
        public static JChannel channel() throws Exception {
            JChannel channel = new JChannel(Util.getTestStack());
            channel.connect("test");
            return channel;
        }

        @Bean
        public static DefaultAdvisorAutoProxyCreator aop() {
            return new DefaultAdvisorAutoProxyCreator();
        }
    }

    @Configuration
    @Import(BaseTestSpringConfig.class)
    public static class ValidTestSpringConfig {
        @Bean
        @Scope(proxyMode=ScopedProxyMode.TARGET_CLASS)
        public static TestBean testBean() {
            return new TestBean();
        }
    }

    @Configuration
    @Import(BaseTestSpringConfig.class)
    public static class InvalidTestSpringConfig {
        @Bean
        @Scope(proxyMode=ScopedProxyMode.TARGET_CLASS)
        public static InvalidTestBean testBean() {
            return new InvalidTestBean();
        }
    }

    @Configuration
    @Import(BaseTestSpringConfig.class)
    public static class InvalidOpcTestSpringConfig {
        @Bean
        @Scope(proxyMode=ScopedProxyMode.TARGET_CLASS)
        public static InvalidOpcTestBean testBean() {
            return new InvalidOpcTestBean();
        }
    }
}
