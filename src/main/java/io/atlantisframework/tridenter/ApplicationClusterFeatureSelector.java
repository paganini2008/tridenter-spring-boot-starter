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
package io.atlantisframework.tridenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.AnnotationMetadata;

import io.atlantisframework.tridenter.election.ApplicationClusterLeaderConfig;
import io.atlantisframework.tridenter.gateway.GatewayAutoConfiguration;
import io.atlantisframework.tridenter.monitor.HealthIndicatorConfig;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastConfig;
import io.atlantisframework.tridenter.utils.ApplicationContextCommonConfig;
import io.atlantisframework.tridenter.xa.XaConfig;

/**
 * 
 * ApplicationClusterFeatureSelector
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public class ApplicationClusterFeatureSelector implements ImportSelector, EnvironmentAware {

	private static final String APPLICATION_CLUSTER_REDIS_PUBSUB_CHANNEL = "APPLICATION_CLUSTER_REDIS_PUBSUB_CHANNEL";
	private static final String APPLICATION_CLUSTER_REDIS_PUBSUB_CHANNEL_SUFFIX = "-redis-pubsub";

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		List<String> importedClassNames = new ArrayList<String>();
		importedClassNames.add(ApplicationContextCommonConfig.class.getName());

		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(EnableApplicationCluster.class.getName()));
		if (annotationAttributes.getBoolean("enableMulticast")) {
			importedClassNames.addAll(Arrays.asList(ApplicationMulticastConfig.class.getName()));
		}
		if (annotationAttributes.getBoolean("enableLeaderElection")) {
			importedClassNames.add(ApplicationClusterLeaderConfig.class.getName());
		}
		if (annotationAttributes.getBoolean("enableGateway")) {
			importedClassNames.add(GatewayAutoConfiguration.class.getName());
		}
		if (annotationAttributes.getBoolean("enableMonitor")) {
			importedClassNames.add(HealthIndicatorConfig.class.getName());
		}
		if (annotationAttributes.getBoolean("enableXA")) {
			importedClassNames.add(XaConfig.class.getName());
		}
		return importedClassNames.toArray(new String[0]);
	}

	@Override
	public void setEnvironment(Environment environment) {
		final String applicationClusterName = environment.getRequiredProperty("spring.application.cluster.name");
		((ConfigurableEnvironment) environment).getPropertySources()
				.addLast(new MapPropertySource(APPLICATION_CLUSTER_REDIS_PUBSUB_CHANNEL, Collections.singletonMap(
						"spring.redis.messager.pubsub.channel", applicationClusterName + APPLICATION_CLUSTER_REDIS_PUBSUB_CHANNEL_SUFFIX)));
	}

}
