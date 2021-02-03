package org.springtribe.framework.cluster.http;

import org.springframework.context.ApplicationListener;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.election.ApplicationClusterFollowerEvent;
import org.springtribe.framework.cluster.election.LeaderNotFoundException;

/**
 * 
 * LeaderRoutingAllocator
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class LeaderRoutingAllocator implements RoutingAllocator, ApplicationListener<ApplicationClusterFollowerEvent> {

	private ApplicationInfo leaderInfo;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		this.leaderInfo = event.getLeaderInfo();
	}

	@Override
	public String allocateHost(String provider, String path) {
		if (leaderInfo == null) {
			throw new LeaderNotFoundException();
		}
		return leaderInfo.getApplicationContextPath() + path;
	}

}
