package indi.atlantis.framework.tridenter.election;

import org.springframework.context.ApplicationContext;

import indi.atlantis.framework.tridenter.ApplicationClusterEvent;
import indi.atlantis.framework.tridenter.LeaderState;

/**
 * 
 * ApplicationClusterLeaderEvent
 * 
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationClusterLeaderEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -2932470508571995512L;

	public ApplicationClusterLeaderEvent(ApplicationContext applicationContext) {
		this(applicationContext, LeaderState.LEADABLE);
	}

	public ApplicationClusterLeaderEvent(ApplicationContext applicationContext, LeaderState leaderState) {
		super(applicationContext, leaderState);
	}

}
