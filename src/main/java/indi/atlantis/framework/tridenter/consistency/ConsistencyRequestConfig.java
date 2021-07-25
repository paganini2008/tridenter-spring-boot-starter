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
package indi.atlantis.framework.tridenter.consistency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.multithreads.Clock;

import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;

/**
 * 
 * ConsistencyRequestConfig
 *
 * @author Fred Feng
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
