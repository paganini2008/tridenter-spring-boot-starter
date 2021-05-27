package indi.atlantis.framework.tridenter.monitor;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import indi.atlantis.framework.tridenter.http.RestClientConfig;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastConfig;

/**
 * 
 * HealthIndicatorConfig
 *
 * @author Fred Feng
 * @version 1.0
 */
@ConditionalOnClass(AbstractHealthIndicator.class)
@Configuration
public class HealthIndicatorConfig {

	@Bean("applicationClusterHealth")
	@ConditionalOnBean(ApplicationMulticastConfig.class)
	public ApplicationClusterHealthIndicator applicationClusterHealth() {
		return new ApplicationClusterHealthIndicator();
	}

	@Bean("applicationClusterTaskExecutorHealth")
	@ConditionalOnBean(name = "applicationClusterTaskExecutor")
	public TaskExecutorHealthIndicator applicationClusterTaskExecutorHealth() {
		return new TaskExecutorHealthIndicator();
	}

	@Bean("restClientHealth")
	@ConditionalOnBean(RestClientConfig.class)
	public RestClientHealthIndicator restClientHealth() {
		return new RestClientHealthIndicator();
	}

}
