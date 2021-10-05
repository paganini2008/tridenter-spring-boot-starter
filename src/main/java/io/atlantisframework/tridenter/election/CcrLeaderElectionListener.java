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
package io.atlantisframework.tridenter.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springdessert.reditools.RedisComponentNames;

import io.atlantisframework.tridenter.ApplicationClusterContext;
import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.ClusterConstants;
import io.atlantisframework.tridenter.InstanceId;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastGroup;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CcrLeaderElectionListener
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class CcrLeaderElectionListener implements ApplicationMulticastListener, ApplicationContextAware, LeaderElectionListener {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${atlantis.framework.tridenter.election.ccr.minimumParticipants:3}")
	private int minimumParticipants;

	@Qualifier(RedisComponentNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private LeaderElection leaderElection;

	@Autowired
	private ApplicationClusterContext applicationClusterContext;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private LeaderRecovery leaderRecovery;

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
			log.info("This is the follower of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterFollowerEvent.class.getName());
			instanceId.setLeaderInfo(leaderInfo);
			log.info("Leader's info: " + leaderInfo);

			final String key = ClusterConstants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
			redisTemplate.opsForList().leftPush(key, instanceId.getApplicationInfo());
		} else {
			final int nCandidates = applicationMulticastGroup.countOfCandidate();
			if (nCandidates >= minimumParticipants) {
				leaderElection.launch();
			}
		}
	}

	@Override
	public synchronized void onInactive(ApplicationInfo applicationInfo) {
		if (applicationClusterContext.getLeaderInfo() != null && applicationClusterContext.getLeaderInfo().equals(applicationInfo)) {
			final String key = ClusterConstants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
			redisTemplate.opsForList().remove(key, 1, instanceId.getApplicationInfo());

			instanceId.setLeaderInfo(null);

			leaderRecovery.recover(applicationInfo);
		}
	}

}
