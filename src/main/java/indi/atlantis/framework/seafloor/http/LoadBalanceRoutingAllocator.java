package indi.atlantis.framework.seafloor.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;

import indi.atlantis.framework.seafloor.ApplicationInfo;
import indi.atlantis.framework.seafloor.LoadBalancer;
import indi.atlantis.framework.seafloor.election.ApplicationClusterFollowerEvent;
import indi.atlantis.framework.seafloor.election.LeaderNotFoundException;
import indi.atlantis.framework.seafloor.multicast.RegistryCenter;

/**
 * 
 * LoadBalanceRoutingAllocator
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public class LoadBalanceRoutingAllocator implements RoutingAllocator, ApplicationListener<ApplicationClusterFollowerEvent> {

	@Autowired
	private RegistryCenter registryCenter;

	@Qualifier("applicationClusterLoadBalancer")
	@Autowired
	private LoadBalancer loadBalancer;

	private ApplicationInfo leaderInfo;

	@Override
	public String allocateHost(String provider, String path) {
		ApplicationInfo selectedApplication;
		if (provider.equalsIgnoreCase(LEADER)) {
			if (leaderInfo == null) {
				throw new LeaderNotFoundException(LEADER);
			}
			selectedApplication = leaderInfo;
		} else if (provider.equals(ALL)) {
			List<ApplicationInfo> candidates = registryCenter.getApplications();
			selectedApplication = loadBalancer.select(path, candidates);
		} else {
			List<ApplicationInfo> candidates = registryCenter.getApplications(provider);
			selectedApplication = loadBalancer.select(path, candidates);
		}
		if (selectedApplication == null) {
			throw new RoutingPolicyException("Invalid provider name: " + provider);
		}
		return selectedApplication.getApplicationContextPath() + path;
	}

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		this.leaderInfo = event.getLeaderInfo();
	}

}
