package org.springtribe.framework.cluster.election;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.InstanceId;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastEvent;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;

import com.github.paganini2008.devtools.ArrayUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationLeaderElection
 *
 * @author Jimmy Hoff
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
