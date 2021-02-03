package org.springtribe.framework.cluster.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springtribe.framework.cluster.ApplicationClusterContext;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.HealthState;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup;

import com.github.paganini2008.devtools.io.FileUtils;

/**
 * 
 * ApplicationClusterHealthIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterHealthIndicator extends AbstractHealthIndicator {

	@Autowired
	private ApplicationClusterContext applicationClusterContext;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		HealthState healthState = applicationClusterContext.getHealthState();
		if (healthState == HealthState.FATAL) {
			builder.down();
		} else {
			builder.up();
		}
		builder.withDetail("healthState", healthState);
		builder.withDetail("candidates", applicationMulticastGroup.countOfCandidate());
		builder.withDetail("leader", getLeaderInfo());
		builder.withDetail("totalMemory", FileUtils.formatSize(Runtime.getRuntime().totalMemory()));
		builder.withDetail("usedMemory", FileUtils.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
	}

	private ApplicationInfo getLeaderInfo() {
		for (ApplicationInfo candidate : applicationMulticastGroup.getCandidates()) {
			if (candidate.isLeader()) {
				return candidate;
			}
		}
		return null;
	}

}
