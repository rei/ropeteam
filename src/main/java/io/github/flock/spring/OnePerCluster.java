package io.github.flock.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this method is only actually invoked on one cluster member. If this member is not
 * the lock holder for this method it will return immediately.
 * 
 * Primary use case is for @Scheduled methods that you only want one server running in your cluster. 
 * 
 * Will automatically failover if lock holder dies. 
 * 
 * @author Jeff Skjonsby
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnePerCluster {}
