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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import indi.atlantis.framework.tridenter.election.ApplicationClusterLeaderConfig;
import indi.atlantis.framework.tridenter.gateway.GatewayAutoConfiguration;
import indi.atlantis.framework.tridenter.monitor.HealthIndicatorConfig;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastConfig;
import indi.atlantis.framework.tridenter.utils.ApplicationUtilityConfig;

/**
 * 
 * ApplicationClusterConfigurationSelector
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationClusterConfigurationSelector implements ImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		List<String> importedClassNames = new ArrayList<String>();
		importedClassNames.add(ApplicationUtilityConfig.class.getName());

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
		return importedClassNames.toArray(new String[0]);
	}
}
