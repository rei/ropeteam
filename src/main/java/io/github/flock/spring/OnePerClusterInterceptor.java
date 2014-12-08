package io.github.flock.spring;

import io.github.flock.OncePerClusterExecutor;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

public class OnePerClusterInterceptor extends AbstractPointcutAdvisor implements MethodInterceptor {
    private static final long serialVersionUID = 6480088587818109165L;

    private static final StaticMethodMatcherPointcut pointcut = new StaticMethodMatcherPointcut() {
        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return method.isAnnotationPresent(OnePerCluster.class) && method.getReturnType().equals(Void.TYPE);
        }
    };

    private OncePerClusterExecutor executor;

    public OnePerClusterInterceptor(OncePerClusterExecutor executor) {
        this.executor = executor;
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (executor.canExecute(invocation.getMethod().toString())) {
            return invocation.proceed();
        }
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
