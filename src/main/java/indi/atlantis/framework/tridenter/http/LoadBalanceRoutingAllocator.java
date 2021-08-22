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
package indi.atlantis.framework.tridenter.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.StringUtils;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.LoadBalancer;
import indi.atlantis.framework.tridenter.election.ApplicationClusterRefreshedEvent;
import indi.atlantis.framework.tridenter.election.LeaderNotFoundException;
import indi.atlantis.framework.tridenter.multicast.RegistryCenter;

/**
 * 
 * LoadBalanceRoutingAllocator
 *
 * @author Fred Feng
 * 
 * @since 2.0.1
 */
public class LoadBalanceRoutingAllocator implements RoutingAllocator, ApplicationListener<ApplicationClusterRefreshedEvent> {

	@Autowired
	private RegistryCenter registryCenter;

	@Qualifier("applicationClusterLoadBalancer")
	@Autowired
	private LoadBalancer loadBalancer;

	private ApplicationInfo leaderInfo;

	@Override
	public String allocateHost(String provider, String path, Request request) {
		if (StringUtils.isBlank(provider)) {
			return path;
		}
		ApplicationInfo selectedApplication = null;
		List<ApplicationInfo> candidates = null;
		switch (provider) {
		case LEADER:
			if (leaderInfo == null) {
				throw new LeaderNotFoundException(LEADER);
			}
			selectedApplication = leaderInfo;
			break;
		case ALL:
			candidates = registryCenter.getApplications();
			selectedApplication = loadBalancer.select(null, candidates, path);
			break;
		default:
			candidates = registryCenter.getApplications(provider);
			selectedApplication = loadBalancer.select(provider, candidates, path);
			break;
		}
		if (selectedApplication == null) {
			throw new RoutingPolicyException("Invalid provider name: " + provider);
		}
		return selectedApplication.getApplicationContextPath() + path;
	}

	@Override
	public void onApplicationEvent(ApplicationClusterRefreshedEvent event) {
		this.leaderInfo = event.getLeaderInfo();
	}

}
