/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.atlantisframework.tridenter.utils;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.springdessert.reditools.common.RedisTtlKeeper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationContextCommonConfig
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Contact.class)
@Import({ ApplicationContextUtils.class, BeanExpressionUtils.class, BeanLazyInitializer.class })
public class ApplicationContextCommonConfig {

	@Value("${spring.application.cluster.common.taskExecutorThreads:-1}")
	private int taskExecutorThreads;

	@Value("${spring.application.cluster.common.taskSchedulerThreads:-1}")
	private int taskSchedulerThreads;

	@ConditionalOnMissingBean(name = "applicationClusterTaskExecutor")
	@Bean
	public ThreadPoolTaskExecutor applicationClusterTaskExecutor() {
		final int nThreads = taskExecutorThreads > 0 ? taskExecutorThreads : Runtime.getRuntime().availableProcessors() * 2;
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(nThreads);
		taskExecutor.setMaxPoolSize(nThreads);
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		taskExecutor.setThreadNamePrefix("spring-application-cluster-threads-");
		return taskExecutor;
	}

	@ConditionalOnMissingBean(name = "applicationClusterTaskScheduler")
	@Bean
	public ThreadPoolTaskScheduler applicationClusterTaskScheduler() {
		final int nThreads = taskExecutorThreads > 0 ? taskExecutorThreads : Runtime.getRuntime().availableProcessors() * 2;
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(nThreads);
		threadPoolTaskScheduler.setThreadNamePrefix("spring-application-cluster-scheduler-threads-");
		threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
		threadPoolTaskScheduler.setErrorHandler(defaultErrorHandler());
		return threadPoolTaskScheduler;
	}

	@Bean
	public RedisTtlKeeper redisTtlKeeper(RedisConnectionFactory connectionFactory,
			@Qualifier("applicationClusterTaskScheduler") TaskScheduler taskScheduler) {
		return new RedisTtlKeeper(connectionFactory, taskScheduler);
	}

	@Bean
	public ErrorHandler defaultErrorHandler() {
		return new DefaultErrorHandler();
	}

	@Slf4j
	public static class DefaultErrorHandler implements ErrorHandler {

		@Override
		public void handleError(Throwable e) {
			log.error(e.getMessage(), e);
		}

	}

}
