package org.springtribe.framework.cluster.utils;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;

import com.github.paganini2008.devtools.multithreads.PooledThreadFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationUtilityConfig
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Configuration
@Import({ ApplicationContextUtils.class, BeanExpressionUtils.class, BeanLazyInitializer.class })
public class ApplicationUtilityConfig {

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
		taskExecutor.setThreadFactory(new PooledThreadFactory("spring-application-cluster-task-executor-"));
		taskExecutor.setRejectedExecutionHandler(defaultRejectedPolicy());
		return taskExecutor;
	}

	@ConditionalOnMissingBean(name = "applicationClusterTaskScheduler")
	@Bean(destroyMethod = "shutdown")
	public ThreadPoolTaskScheduler applicationClusterTaskScheduler() {
		final int nThreads = taskExecutorThreads > 0 ? taskExecutorThreads : Runtime.getRuntime().availableProcessors() * 2;
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(nThreads);
		threadPoolTaskScheduler.setThreadFactory(new PooledThreadFactory("spring-application-cluster-task-scheduler-"));
		threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
		threadPoolTaskScheduler.setRejectedExecutionHandler(defaultRejectedPolicy());
		threadPoolTaskScheduler.setErrorHandler(defaultErrorHandler());
		return threadPoolTaskScheduler;
	}

	@ConditionalOnMissingBean
	@Bean
	public RejectedExecutionHandler defaultRejectedPolicy() {
		return new ThreadPoolExecutor.AbortPolicy();
	}

	@ConditionalOnMissingBean
	@Bean
	public ErrorHandler defaultErrorHandler() {
		return new DefaultErrorHandler();
	}

	@ConditionalOnMissingBean
	@Bean
	public Contact clusterContact() {
		return new Contact();
	}

	@Slf4j
	public static class DefaultErrorHandler implements ErrorHandler {

		@Override
		public void handleError(Throwable e) {
			log.error(e.getMessage(), e);
		}

	}

}
