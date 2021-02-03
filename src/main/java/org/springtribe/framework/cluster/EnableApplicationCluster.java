package org.springtribe.framework.cluster;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 
 * EnableApplicationCluster
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ApplicationClusterConfigurationSelector.class)
public @interface EnableApplicationCluster {

	boolean enableMulticast() default true;

	boolean enableLeaderElection() default false;

	boolean enableGateway() default false;

	boolean enableMonitor() default false;

}
