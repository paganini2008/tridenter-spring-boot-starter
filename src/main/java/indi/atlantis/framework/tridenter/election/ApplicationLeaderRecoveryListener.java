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
 * @author Jimmy Hoff
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
			applicationContext.publishEvent(new ApplicationClusterLeaderEvent(applicationContext, LeaderState.UNLEADABLE));
			
			leaderRecovery.recover(formerLeader);
		}
	}

}
