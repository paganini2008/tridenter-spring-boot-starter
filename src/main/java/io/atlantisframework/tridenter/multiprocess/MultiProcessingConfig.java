/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter.multiprocess;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springdessert.reditools.common.ProcessLock;
import com.github.paganini2008.springdessert.reditools.common.RedisProcessLock;

import io.atlantisframework.tridenter.ClusterConstants;

/**
 * 
 * MultiProcessingConfig
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@ConditionalOnClass(ProceedingJoinPoint.class)
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.multiprocessing.enabled", havingValue = "true", matchIfMissing = true)
public class MultiProcessingConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public ProcessLockTtlKeeper processLockTtlKeeper() {
		return new ProcessLockTtlKeeper();
	}

	@ConditionalOnMissingBean(name = "multiprocessingLock")
	@Bean
	public ProcessLock multiprocessingLock(RedisConnectionFactory connectionFactory,
			@Value("${spring.application.cluster.multiprocessing.lock.maxPermits:16}") int maxPermits) {
		final String lockName = ClusterConstants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":pool";
		return new RedisProcessLock(lockName, connectionFactory, 60, maxPermits);
	}

	@ConditionalOnMissingBean
	@Bean
	public DelayQueue delayQueue() {
		return new CachedDelayQueue();
	}

	@Bean(destroyMethod = "shutdown")
	public ProcessPool processPool() {
		return new ProcessPoolExecutor();
	}

	@Bean
	public ScheduledProcessPool scheduledProcessPool() {
		return new ScheduledProcessPoolExecutor();
	}

	@Bean
	public ProcessPoolTaskListener processPoolTaskListener() {
		return new ProcessPoolTaskListener();
	}

	@Bean
	public MultiProcessingInterpreter multiProcessingInterpreter() {
		return new MultiProcessingInterpreter();
	}

	@Bean
	public ParallelizingCallInterpreter parallelizingCallInterpreter() {
		return new ParallelizingCallInterpreter();
	}

	@Bean
	public MultiSchedulingInterpreter multiSchedulingInterpreter() {
		return new MultiSchedulingInterpreter();
	}

	@Bean
	public MultiProcessingMethodInspector multiProcessingMethodInspector() {
		return new MultiProcessingMethodInspector();
	}

	@Bean
	public MultiProcessingCallbackListener multiProcessingCallbackListener() {
		return new MultiProcessingCallbackListener();
	}

	@Bean
	public MultiProcessingCompletionListener multiProcessingCompletionListener() {
		return new MultiProcessingCompletionListener();
	}

	@Bean
	public InvocationBarrier invocationBarrier() {
		return new InvocationBarrier();
	}

}
