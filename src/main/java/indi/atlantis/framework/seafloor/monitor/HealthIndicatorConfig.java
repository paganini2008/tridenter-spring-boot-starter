package indi.atlantis.framework.seafloor.monitor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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

	@Bean("applicationCluster")
	@ConditionalOnBean(ApplicationMulticastConfig.class)
	public ApplicationClusterHealthIndicator applicationClusterHealthIndicator() {
		return new ApplicationClusterHealthIndicator();
	}

	@Bean("taskExecutorHealthIndicator")
	@ConditionalOnBean(value = ThreadPoolTaskExecutor.class, name = "applicationClusterTaskExecutor")
	public TaskExecutorHealthIndicator taskExecutorHealthIndicator() {
		return new TaskExecutorHealthIndicator();
	}

	@Bean("httpStatistic")
	@ConditionalOnBean(RestClientConfig.class)
	public HttpStatisticHealthIndicator httpStatisticHealthIndicator() {
		return new HttpStatisticHealthIndicator();
	}

}
