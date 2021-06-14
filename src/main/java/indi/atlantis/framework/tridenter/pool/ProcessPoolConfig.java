package indi.atlantis.framework.tridenter.pool;

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
 * ProcessPoolConfig
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.pool.enabled", havingValue = "true")
public class ProcessPoolConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.cluster.pool.size:16}")
	private int poolSize;

	@Bean
	public RedisCounter redisCounter(RedisConnectionFactory redisConnectionFactory, TtlKeeper ttlKeeper) {
		final String fullName = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":pool";
		RedisCounter redisCounter = new RedisCounter(fullName, redisConnectionFactory);
		redisCounter.keepAlive(ttlKeeper, 5, TimeUnit.SECONDS);
		return redisCounter;
	}

	@ConditionalOnMissingBean(SharedLatch.class)
	@Bean
	public SharedLatch sharedLatch(RedisCounter redisCounter) {
		return new RedisSharedLatch(redisCounter, poolSize);
	}

	@ConditionalOnMissingBean(DelayQueue.class)
	@Bean
	public DelayQueue delayQueue() {
		return new CachedDelayQueue();
	}

	@Bean(destroyMethod = "shutdown")
	public ProcessPool processPool() {
		return new ProcessPoolExecutor();
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
	public ParallelizingCallInterpreter callParallelizingInterpreter() {
		return new ParallelizingCallInterpreter();
	}

	@Bean
	public MultiProcessingMethodDetector multiProcessingMethodDetector() {
		return new MultiProcessingMethodDetector();
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
