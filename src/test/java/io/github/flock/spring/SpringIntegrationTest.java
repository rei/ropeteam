package io.github.flock.spring;

import static org.junit.Assert.assertEquals;
import io.github.flock.EventPublisher;

import org.jgroups.JChannel;
import org.jgroups.util.Util;
import org.junit.Test;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

public class SpringIntegrationTest {

    @Test
    @SuppressWarnings("resource")
    public void interceptsOncePerClusterAnnotatedStuff() throws InterruptedException {
        ApplicationContext context1 = new AnnotationConfigApplicationContext(TestSpringConfig.class);
        ApplicationContext context2 = new AnnotationConfigApplicationContext(TestSpringConfig.class);
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
    @Import(FlockConfig.class)
    public static class TestSpringConfig {
        @Bean
        public static JChannel channel() throws Exception {
            JChannel channel = new JChannel(Util.getTestStack());
            channel.connect("test");
            return channel;
        }
        
        @Bean
        @Scope(proxyMode=ScopedProxyMode.TARGET_CLASS)
        public static TestBean testBean() {
            return new TestBean();
        }
        
        @Bean
        public static DefaultAdvisorAutoProxyCreator aop() {
            return new DefaultAdvisorAutoProxyCreator();
        }
    }
}
