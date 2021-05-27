package indi.atlantis.framework.tridenter.http;

import org.springframework.context.ApplicationListener;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.election.ApplicationClusterFollowerEvent;
import indi.atlantis.framework.tridenter.election.LeaderNotFoundException;

/**
 * 
 * LeaderRoutingAllocator
 * 
 * @author Fred Feng
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
	public String allocateHost(String provider, String path, Request request) {
		if (leaderInfo == null) {
			throw new LeaderNotFoundException();
		}
		return leaderInfo.getApplicationContextPath() + path;
	}

}
