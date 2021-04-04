package indi.atlantis.framework.tridenter;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import indi.atlantis.framework.tridenter.election.ApplicationClusterRefreshedEvent;

/**
 * 
 * ApplicationClusterContext
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterContext implements SmartApplicationListener {

	private ApplicationInfo leaderInfo;
	private volatile LeaderState leaderState = LeaderState.DOWN;

	public ApplicationInfo getLeaderInfo() {
		return leaderInfo;
	}

	public LeaderState getLeaderState() {
		return leaderState;
	}

	public void setLeaderState(LeaderState leaderState) {
		this.leaderState = leaderState;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		ApplicationClusterEvent applicationClusterEvent = (ApplicationClusterEvent) event;
		this.leaderState = applicationClusterEvent.getLeaderState();

		if (event instanceof ApplicationClusterRefreshedEvent) {
			this.leaderInfo = ((ApplicationClusterRefreshedEvent) event).getLeaderInfo();
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ApplicationClusterRefreshedEvent.class || eventType == ApplicationClusterFatalEvent.class;
	}

}
