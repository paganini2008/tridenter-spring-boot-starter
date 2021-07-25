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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springdessert.reditools.RedisComponentNames;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.Constants;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyLeaderElectionListener
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyLeaderElectionListener implements ApplicationMulticastListener, ApplicationContextAware, LeaderElectionListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.cluster.consistency.leader-election.minimumParticipants:3}")
	private int minimumParticipants;

	@Qualifier(RedisComponentNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private LeaderElection leaderElection;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Autowired
	private LeaderRecovery recoveryCallback;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public synchronized void onActive(ApplicationInfo applicationInfo) {
		if (instanceId.getLeaderInfo() != null) {
			return;
		}
		ApplicationInfo leaderInfo = applicationInfo.getLeaderInfo();
		if (leaderInfo != null) {
			log.info("Join the existed cluster: " + leaderInfo.getClusterName());

			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderInfo));
			log.info("I am the follower of application cluster '{}'. Implement ApplicationListener to listen the event type {}",
					clusterName, ApplicationClusterFollowerEvent.class.getName());
			instanceId.setLeaderInfo(leaderInfo);
			log.info("Leader's info: " + leaderInfo);

			final String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
			redisTemplate.opsForList().leftPush(key, instanceId.getApplicationInfo());
		} else {
			final int channelCount = multicastGroup.countOfCandidate();
			if (channelCount >= minimumParticipants) {
				leaderElection.launch();
			}
		}
	}

	@Override
	public synchronized void onInactive(ApplicationInfo applicationInfo) {
		if (applicationInfo.isLeader()) {
			final String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
			redisTemplate.opsForList().remove(key, 1, instanceId.getApplicationInfo());

			instanceId.setLeaderInfo(null);

			recoveryCallback.recover(applicationInfo);
		}
	}

}
