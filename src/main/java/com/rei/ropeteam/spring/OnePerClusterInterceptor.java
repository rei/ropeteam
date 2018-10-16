package com.rei.ropeteam.spring;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import com.rei.ropeteam.OncePerClusterExecutor;

public class OnePerClusterInterceptor extends AbstractPointcutAdvisor implements MethodInterceptor {
    private static final long serialVersionUID = 6480088587818109165L;

    private static final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            boolean annotationPresent = method.isAnnotationPresent(OnePerCluster.class);
            if (annotationPresent && !method.getReturnType().equals(Void.TYPE)) {
                throw new IllegalArgumentException("@OnePerCluster annotated methods MUST return 'void'!");
            }
            return annotationPresent;
        }
    };

    private OncePerClusterExecutor executor;

    public OnePerClusterInterceptor(OncePerClusterExecutor executor) {
        this.executor = executor;
        setOrder(LOWEST_PRECEDENCE);
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        executor.execute(invocation.getMethod().toString(), () -> invocation.proceed());
        return null;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this;
    }
}
