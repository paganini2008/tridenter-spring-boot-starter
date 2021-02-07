package indi.atlantis.framework.seafloor;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import indi.atlantis.framework.seafloor.election.ApplicationClusterRefreshedEvent;

/**
 * 
 * ApplicationClusterContext
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterContext implements SmartApplicationListener {

	private ApplicationInfo leaderInfo;
	private volatile HealthState healthState = HealthState.UNLEADABLE;

	public ApplicationInfo getLeaderInfo() {
		return leaderInfo;
	}

	public HealthState getHealthState() {
		return healthState;
	}

	public void setHealthState(HealthState clusterState) {
		this.healthState = healthState;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		ApplicationClusterEvent applicationClusterEvent = (ApplicationClusterEvent) event;
		this.healthState = applicationClusterEvent.getHealthState();

		if (event instanceof ApplicationClusterRefreshedEvent) {
			this.leaderInfo = ((ApplicationClusterRefreshedEvent) event).getLeaderInfo();
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ApplicationClusterRefreshedEvent.class || eventType == ApplicationClusterFatalEvent.class;
	}

}
