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
package io.atlantisframework.tridenter.multicast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import io.atlantisframework.tridenter.ApplicationClusterContext;
import io.atlantisframework.tridenter.ApplicationClusterController;
import io.atlantisframework.tridenter.ApplicationClusterLoadBalancer;
import io.atlantisframework.tridenter.ClusterConstants;
import io.atlantisframework.tridenter.DigestInstanceIdGenerator;
import io.atlantisframework.tridenter.InstanceId;
import io.atlantisframework.tridenter.InstanceIdGenerator;
import io.atlantisframework.tridenter.LoadBalancer;
import io.atlantisframework.tridenter.RedisConnectionFailureHandler;
import io.atlantisframework.tridenter.ccr.CcrRequestConfig;
import io.atlantisframework.tridenter.http.RestClientConfig;
import io.atlantisframework.tridenter.multiprocess.MultiProcessingConfig;
import io.atlantisframework.tridenter.utils.EnableRedisClient;

/**
 * 
 * ApplicationMulticastConfig
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@EnableRedisClient
@AutoConfigureBefore({ RestClientConfig.class, MultiProcessingConfig.class, CcrRequestConfig.class })
@Import({ ApplicationMulticastController.class, ApplicationClusterController.class, RestClientConfig.class, MultiProcessingConfig.class,
		CcrRequestConfig.class })
@Configuration
public class ApplicationMulticastConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	@ConditionalOnMissingBean
	public InstanceIdGenerator instanceIdGenerator() {
		return new DigestInstanceIdGenerator();
	}

	@Bean
	public InstanceId instanceId() {
		return new InstanceId();
	}

	@Bean
	public ApplicationClusterContext applicationClusterContext() {
		return new ApplicationClusterContext();
	}

	@Bean
	public ApplicationClusterListenerContainer applicationClusterListenerContainer() {
		return new ApplicationClusterListenerContainer();
	}

	@Bean
	public RedisConnectionFailureHandler redisConnectionFailureHandler() {
		return new RedisConnectionFailureHandler();
	}

	@Bean
	public ApplicationMulticastStarter applicationMulticastStarter() {
		return new ApplicationMulticastStarter();
	}

	@Bean
	public ApplicationMulticastStarterListener applicationMulticastStarterListener() {
		return new ApplicationMulticastStarterListener();
	}

	@Bean
	public ApplicationMessageStarterListener applicationMessageStarterListener() {
		return new ApplicationMessageStarterListener();
	}

	@Bean
	public ApplicationClusterHeartbeatListener applicationClusterHeartbeatListener() {
		return new ApplicationClusterHeartbeatListener();
	}

	@Bean
	public ApplicationMulticastGroup applicationMulticastGroup() {
		return new ApplicationMulticastGroup();
	}

	@ConditionalOnMissingBean(name = "applicationMulticastLoadBalancer")
	@Bean
	public LoadBalancer applicationMulticastLoadBalancer(RedisConnectionFactory connectionFactory) {
		final String name = ClusterConstants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter:multicast";
		return new ApplicationClusterLoadBalancer(name, connectionFactory);
	}

	@Bean
	public MulticastMessageAcker multicastMessageAcker() {
		return new MulticastMessageAcker();
	}

	@Bean
	public LoggingApplicationMulticastListener loggingApplicationMulticastListener() {
		return new LoggingApplicationMulticastListener();
	}

	@Bean
	public RegistryCenter applicationRegistryCenter() {
		return new ApplicationRegistryCenter();
	}

}
