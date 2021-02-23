package indi.atlantis.framework.seafloor.monitor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import indi.atlantis.framework.seafloor.http.RestClientConfig;
import indi.atlantis.framework.seafloor.multicast.ApplicationMulticastConfig;

/**
 * 
 * HealthIndicatorConfig
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
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
