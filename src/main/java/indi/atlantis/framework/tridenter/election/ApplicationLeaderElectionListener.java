package indi.atlantis.framework.tridenter.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;

/**
 * 
 * ApplicationLeaderElectionListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationLeaderElectionListener implements LeaderElectionListener, ApplicationListener<ApplicationMulticastEvent> {

	@Autowired
	private LeaderElection leaderElection;

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent applicationEvent) {
		if (applicationEvent.getMulticastEventType() == MulticastEventType.ON_ACTIVE) {
			leaderElection.onTriggered(applicationEvent);
		}
	}

}
