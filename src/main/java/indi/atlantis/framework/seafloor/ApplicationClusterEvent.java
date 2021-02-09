package indi.atlantis.framework.seafloor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * 
 * Base class for application cluster event
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class ApplicationClusterEvent extends ApplicationContextEvent {

	private static final long serialVersionUID = -9030425105386583374L;

	public ApplicationClusterEvent(ApplicationContext applicationContext, LeaderState leaderState) {
		super(applicationContext);
		this.leaderState = leaderState;
	}

	private final LeaderState leaderState;

	public LeaderState getLeaderState() {
		return leaderState;
	}

}
