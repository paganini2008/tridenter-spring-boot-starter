/**
* Copyright 2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter.multiprocess;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.springdessert.reditools.common.RedisCounter;
import com.github.paganini2008.springdessert.reditools.common.RedisSharedLatch;
import com.github.paganini2008.springdessert.reditools.common.SharedLatch;
import com.github.paganini2008.springdessert.reditools.common.TtlKeeper;

import indi.atlantis.framework.tridenter.Constants;

/**
 * 
 * MultiProcessingConfig
 * 
 * @author Fred Feng
 *
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.multiprocessing.enabled", havingValue = "true", matchIfMissing = true)
public class MultiProcessingConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	public RedisCounter redisCounter(RedisConnectionFactory redisConnectionFactory, TtlKeeper ttlKeeper) {
		final String fullName = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":pool";
		RedisCounter redisCounter = new RedisCounter(fullName, redisConnectionFactory);
		redisCounter.keepAlive(ttlKeeper, 5, TimeUnit.SECONDS);
		return redisCounter;
	}

	@ConditionalOnMissingBean
	@Bean
	public SharedLatch sharedLatch(RedisCounter redisCounter,
			@Value("${spring.application.cluster.multiprocessing.maxPermits:8}") int maxPermits) {
		return new RedisSharedLatch(redisCounter, maxPermits);
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
