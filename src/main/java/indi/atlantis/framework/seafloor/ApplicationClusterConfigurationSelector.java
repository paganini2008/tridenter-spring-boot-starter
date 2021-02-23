package indi.atlantis.framework.seafloor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import indi.atlantis.framework.seafloor.election.ApplicationClusterLeaderConfig;
import indi.atlantis.framework.seafloor.gateway.GatewayConfiguration;
import indi.atlantis.framework.seafloor.monitor.HealthIndicatorConfig;
import indi.atlantis.framework.seafloor.multicast.ApplicationMulticastConfig;
import indi.atlantis.framework.seafloor.utils.ApplicationUtilityConfig;

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
			importedClassNames.add(GatewayConfiguration.class.getName());
		}
		if (annotationAttributes.getBoolean("enableMonitor")) {
			importedClassNames.add(HealthIndicatorConfig.class.getName());
		}
		return importedClassNames.toArray(new String[0]);
	}
}
