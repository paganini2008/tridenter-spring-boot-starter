package indi.atlantis.framework.seafloor.election;

import org.springframework.context.ApplicationContext;

import indi.atlantis.framework.seafloor.ApplicationClusterEvent;
import indi.atlantis.framework.seafloor.HealthState;

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
		this(applicationContext, HealthState.LEADABLE);
	}

	public ApplicationClusterLeaderEvent(ApplicationContext applicationContext, HealthState healthState) {
		super(applicationContext, healthState);
	}

}
