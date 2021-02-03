package org.springtribe.framework.cluster.consistency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springtribe.framework.cluster.multicast.ApplicationMessageListener;

import com.github.paganini2008.devtools.multithreads.Clock;

/**
 * 
 * ConsistencyRequestConfig
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Configuration
public class ConsistencyRequestConfig {

	@ConditionalOnMissingBean(Clock.class)
	@Bean(destroyMethod = "stop")
	public Clock clock() {
		return new Clock();
	}
	
	@Bean
	public Court court() {
		return new Court();
	}

	@Bean
	public ConsistencyRequestContext consistencyRequestContext() {
		return new ConsistencyRequestContext();
	}

	@Bean
	public ConsistencyRequestRound consistencyRequestRound() {
		return new ConsistencyRequestRound();
	}

	@Bean
	public ConsistencyRequestSerial consistencyRequestSerial() {
		return new ConsistencyRequestSerial();
	}

	@Bean
	public ConsistencyRequestSerialCache consistencyRequestSerialCache() {
		return new ConsistencyRequestSerialCache();
	}

	@Bean
	public ApplicationMessageListener consistencyRequestPreparationRequest() {
		return new ConsistencyRequestPreparationRequest();
	}

	@Bean
	public ApplicationMessageListener consistencyRequestPreparationResponse() {
		return new ConsistencyRequestPreparationResponse();
	}

	@Bean
	public ApplicationMessageListener consistencyRequestCommitmentRequest() {
		return new ConsistencyRequestCommitmentRequest();
	}

	@Bean
	public ApplicationMessageListener consistencyRequestCommitmentResponse() {
		return new ConsistencyRequestCommitmentResponse();
	}

	@Bean
	public ApplicationMessageListener consistencyRequestLearningRequest() {
		return new ConsistencyRequestLearningRequest();
	}

	@Bean
	public ApplicationMessageListener consistencyRequestLearningResponse() {
		return new ConsistencyRequestLearningResponse();
	}

}
