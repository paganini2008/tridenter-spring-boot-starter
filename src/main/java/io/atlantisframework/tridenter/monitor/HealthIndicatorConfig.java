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
package io.atlantisframework.tridenter.monitor;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.atlantisframework.tridenter.http.RestClientConfig;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastConfig;

/**
 * 
 * HealthIndicatorConfig
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@ConditionalOnClass(HealthIndicator.class)
@Configuration
public class HealthIndicatorConfig {

	@Bean("applicationClusterHealth")
	@ConditionalOnBean(ApplicationMulticastConfig.class)
	public ApplicationClusterHealthIndicator applicationClusterHealth() {
		return new ApplicationClusterHealthIndicator();
	}

	@Bean("applicationClusterTaskExecutorHealth")
	@ConditionalOnBean(name = "applicationClusterTaskExecutor")
	public TaskExecutorHealthIndicator applicationClusterTaskExecutorHealth() {
		return new TaskExecutorHealthIndicator();
	}

	@Bean("restClientHealth")
	@ConditionalOnBean(RestClientConfig.class)
	public RestClientHealthIndicator restClientHealth() {
		return new RestClientHealthIndicator();
	}

}
