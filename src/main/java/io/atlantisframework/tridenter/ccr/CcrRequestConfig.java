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
package io.atlantisframework.tridenter.ccr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.multithreads.Clock;

import io.atlantisframework.tridenter.multicast.ApplicationMessageListener;

/**
 * 
 * CcrRequestConfig
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Configuration(proxyBeanMethods = false)
public class CcrRequestConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@ConditionalOnMissingBean
	@Bean(destroyMethod = "stop")
	public Clock clock() {
		return new Clock();
	}

	@Bean
	public CcrPlatform ccrPlatform() {
		return new CcrPlatform();
	}

	@Bean
	public CcrRequestLauncher ccrRequestLauncher() {
		return new CcrRequestLauncher();
	}

	@Bean("batchNoGenerator")
	public CcrSerialNoGenerator batchNoGenerator(RedisConnectionFactory redisConnectionFactory) {
		return new CcrSerialNoGenerator("atlantis:framework:tridenter:ccr:batch:" + clusterName, redisConnectionFactory);
	}

	@Bean("serialNoGenerator")
	public CcrSerialNoGenerator serialNoGenerator(RedisConnectionFactory redisConnectionFactory) {
		return new CcrSerialNoGenerator("atlantis:framework:tridenter:ccr:serial:" + clusterName, redisConnectionFactory);
	}

	@Bean
	public CcrRequestLocal ccrRequestLocal() {
		return new CcrRequestLocal();
	}

	@Bean
	public ApplicationMessageListener ccrRequestPreparationListener() {
		return new CcrRequestPreparationListener();
	}

	@Bean
	public ApplicationMessageListener ccrResponsePreparationListener() {
		return new CcrResponsePreparationListener();
	}

	@Bean
	public ApplicationMessageListener ccrRequestCommitmentListener() {
		return new CcrRequestCommitmentListener();
	}

	@Bean
	public ApplicationMessageListener ccrResponseCommitmentListener() {
		return new CcrResponseCommitmentListener();
	}

	@Bean
	public ApplicationMessageListener ccrRequestLearningListener() {
		return new CcrRequestLearningListener();
	}

	@Bean
	public ApplicationMessageListener ccrResponseLearningListener() {
		return new CcrResponseLearningListener();
	}

}
