package org.springtribe.framework.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springtribe.framework.cluster.election.ApplicationClusterLeaderConfig;
import org.springtribe.framework.cluster.gateway.CustomizedRoutingConfig;
import org.springtribe.framework.cluster.monitor.HealthIndicatorConfig;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastConfig;
import org.springtribe.framework.cluster.utils.ApplicationUtilityConfig;

/**
 * 
 * ApplicationClusterConfigurationSelector
 *
 * @author Jimmy Hoff
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
			importedClassNames.add(CustomizedRoutingConfig.class.getName());
		}
		if (annotationAttributes.getBoolean("enableMonitor")) {
			importedClassNames.add(HealthIndicatorConfig.class.getName());
		}
		return importedClassNames.toArray(new String[0]);
	}
}
