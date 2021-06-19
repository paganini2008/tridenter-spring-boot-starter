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
package indi.atlantis.framework.tridenter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.retry.support.RetryTemplate;

import com.github.paganini2008.springdessert.reditools.common.ConnectionFailureHandler;
import com.github.paganini2008.springdessert.reditools.common.RedisKeepAliveResolver;

import indi.atlantis.framework.tridenter.http.RetryTemplateFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RedisConnectionFailureHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class RedisConnectionFailureHandler implements ConnectionFailureHandler, ApplicationContextAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisKeepAliveResolver redisKeepAliveResolver;

	@Autowired
	private RetryTemplateFactory retryTemplateFactory;

	private ApplicationContext applicationContext;

	@Override
	public void handleException(Throwable e) {
		log.warn("RedisConnection Refused");
		final RetryTemplate retryTemplate = retryTemplateFactory.setRetryPolicy(3).createObject();
		retryTemplate.execute(context -> {
			return redisKeepAliveResolver.ping();
		}, context -> {
			Throwable reason = context.getLastThrowable();
			if (reason != null) {
				log.error(reason.getMessage(), reason);
			}
			redisKeepAliveResolver.addListener(RedisConnectionFailureHandler.this);
			applicationContext.publishEvent(new ApplicationClusterFatalEvent(applicationContext, reason));
			log.warn("Application cluster '{}' will be unserviceable due to redisConnection refused.", clusterName);
			return "";
		});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
