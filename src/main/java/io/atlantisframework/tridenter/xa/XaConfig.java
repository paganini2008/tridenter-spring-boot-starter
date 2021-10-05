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
package io.atlantisframework.tridenter.xa;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.github.paganini2008.springdessert.reditools.common.ProcessLock;
import com.github.paganini2008.springdessert.reditools.common.RedisAtomicLongSequence;
import com.github.paganini2008.springdessert.reditools.common.RedisProcessLock;

import io.atlantisframework.tridenter.ClusterConstants;
import io.atlantisframework.tridenter.http.RequestInterceptor;

/**
 * 
 * XaConfig
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@ConditionalOnClass(ProceedingJoinPoint.class)
@Configuration
@ConditionalOnProperty(value = "spring.application.cluster.xa.enabled", havingValue = "true", matchIfMissing = true)
public class XaConfig {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.cluster.xa.taskExecutorThreads:-1}")
	private int taskExecutorThreads;

	@ConditionalOnMissingBean(name = "xaProcessLock")
	@Bean
	public ProcessLock xaProcessLock(RedisConnectionFactory connectionFactory,
			@Value("${spring.application.cluster.xa.lock.maxPermits:1}") int maxPermits) {
		final String lockName = ClusterConstants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":xa";
		return new RedisProcessLock(lockName, connectionFactory, 60, maxPermits);
	}

	@Bean
	public XaInterpreter xaInterpreter() {
		return new XaInterpreter();
	}

	@ConditionalOnMissingBean
	@Bean
	public XaMessageAckPredicate xaMessageAckPredicate() {
		return new XaStateChangePredicate();
	}

	@ConditionalOnMissingBean(name = "xaSerialGen")
	@Bean
	public RedisAtomicLongSequence xaSerialGen(RedisConnectionFactory connectionFactory) {
		return new RedisAtomicLongSequence(ClusterConstants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":xa:serial", connectionFactory);
	}

	@Bean
	public RequestInterceptor xaRequestInterceptor() {
		return new XaRequestInterceptor();
	}

	@ConditionalOnMissingBean
	@Bean
	public XaMessageSender xaMessageSender() {
		return new RedisXaMessageSender();
	}

	@Bean
	public XaResourceManager xaResourceManager() {
		return new XaResourceManager();
	}

	@Bean
	public XaManager xaManager() {
		return new XaManager();
	}

	/**
	 * 
	 * XaStateChangeListenerContainer
	 *
	 * @author Fred Feng
	 *
	 * @since 2.0.4
	 */
	@Configuration
	public static class XaStateChangeListenerContainer implements BeanPostProcessor {

		@Autowired
		private XaMessageAckPredicate xaMessagePredicate;

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			if (bean instanceof XaStateChangeListener) {
				xaMessagePredicate.addListener((XaStateChangeListener) bean);
			}
			return bean;
		}

	}

	/**
	 * 
	 * WebMvcConfig
	 *
	 * @author Fred Feng
	 *
	 * @since 2.0.4
	 */
	@Configuration
	public static class WebMvcConfig implements WebMvcConfigurer {

		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(new XaId());
		}

	}

}
