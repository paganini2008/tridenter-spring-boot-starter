package indi.atlantis.framework.tridenter.gateway;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import indi.atlantis.framework.tridenter.EnableApplicationCluster;

/**
 * 
 * EnableGateway
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableApplicationCluster(enableLeaderElection = true)
@Import(GatewayAutoConfiguration.class)
public @interface EnableGateway {
}
