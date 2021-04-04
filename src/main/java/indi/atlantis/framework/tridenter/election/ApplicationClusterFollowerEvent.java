package indi.atlantis.framework.tridenter.election;

import org.springframework.context.ApplicationContext;

import indi.atlantis.framework.tridenter.ApplicationClusterEvent;
import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.LeaderState;

/**
 * 
 * ApplicationClusterFollowerEvent
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterFollowerEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = 9109166626001674260L;

	public ApplicationClusterFollowerEvent(ApplicationContext context, ApplicationInfo leader) {
		super(context, LeaderState.UP);
		this.leader = leader;
	}

	private final ApplicationInfo leader;

	public ApplicationInfo getLeaderInfo() {
		return leader;
	}

}
