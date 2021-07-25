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
package indi.atlantis.framework.tridenter.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import com.github.paganini2008.devtools.ArrayUtils;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationLeaderElection
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ApplicationLeaderElection implements LeaderElection, ApplicationContextAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	private ApplicationContext applicationContext;

	@Override
	public synchronized void launch() {
		if (instanceId.getLeaderInfo() != null) {
			return;
		}
		ApplicationInfo[] candidates = applicationMulticastGroup.getCandidates();
		if (ArrayUtils.isEmpty(candidates)) {
			throw new LeaderNotFoundException("No candidates for election");
		}
		ApplicationInfo leader;
		ApplicationInfo self = instanceId.getApplicationInfo();
		if ((leader = candidates[0]).equals(self)) {
			applicationContext.publishEvent(new ApplicationClusterLeaderEvent(applicationContext));
			log.info("This is the leader of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leader));
			log.info("This is the follower of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterFollowerEvent.class.getName());
		}
		leader.setLeader(true);
		instanceId.setLeaderInfo(leader);
		log.info("Current leader: " + leader);

		applicationContext.publishEvent(new ApplicationClusterRefreshedEvent(applicationContext, leader));
	}

	@Override
	public void onTriggered(ApplicationEvent applicationEvent) {
		ApplicationMulticastEvent applicationMulticastEvent = (ApplicationMulticastEvent) applicationEvent;
		MulticastEventType eventType = applicationMulticastEvent.getMulticastEventType();
		if (eventType == MulticastEventType.ON_ACTIVE) {
			launch();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
