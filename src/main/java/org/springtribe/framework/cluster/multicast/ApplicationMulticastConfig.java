package org.springtribe.framework.cluster.multicast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springtribe.framework.cluster.ApplicationClusterContext;
import org.springtribe.framework.cluster.ApplicationClusterController;
import org.springtribe.framework.cluster.ApplicationClusterLoadBalancer;
import org.springtribe.framework.cluster.Constants;
import org.springtribe.framework.cluster.DefaultInstanceIdGenerator;
import org.springtribe.framework.cluster.InstanceId;
import org.springtribe.framework.cluster.InstanceIdGenerator;
import org.springtribe.framework.cluster.LoadBalancer;
import org.springtribe.framework.cluster.RedisConnectionFailureHandler;
import org.springtribe.framework.cluster.consistency.ConsistencyRequestConfig;
import org.springtribe.framework.cluster.http.RestClientConfig;
import org.springtribe.framework.cluster.pool.ProcessPoolConfig;

/**
 * 
 * ApplicationMulticastConfig
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Configuration
@AutoConfigureBefore({ RestClientConfig.class, ProcessPoolConfig.class, ConsistencyRequestConfig.class })
@Import({ ApplicationMulticastController.class, ApplicationClusterController.class, RestClientConfig.class, ProcessPoolConfig.class,
		ConsistencyRequestConfig.class })
public class ApplicationMulticastConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Bean
	@ConditionalOnMissingBean
	public InstanceIdGenerator instanceIdGenerator() {
		return new DefaultInstanceIdGenerator();
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
	public ApplicationMessageStarterListener applicationMessageListener() {
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
		final String name = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":counter:multicast";
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
