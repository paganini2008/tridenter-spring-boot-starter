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
package indi.atlantis.framework.tridenter.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.LeaderState;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationLeaderRecoveryListener
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ApplicationLeaderRecoveryListener implements ApplicationListener<ApplicationMulticastEvent> {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private LeaderRecovery leaderRecovery;

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent applicationEvent) {
		if (applicationEvent.getMulticastEventType() == MulticastEventType.ON_INACTIVE
				&& applicationEvent.getApplicationInfo().isLeader()) {
			log.info("Leader of application cluster '{}' is expired.", clusterName);
			ApplicationInfo formerLeader = instanceId.getLeaderInfo();
			instanceId.setLeaderInfo(null);

			ApplicationContext applicationContext = applicationEvent.getApplicationContext();
			applicationContext.publishEvent(new ApplicationClusterLeaderEvent(applicationContext, LeaderState.DOWN));
			
			leaderRecovery.recover(formerLeader);
		}
	}

}
