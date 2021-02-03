package org.springtribe.framework.cluster.election;

import org.springframework.context.ApplicationContext;
import org.springtribe.framework.cluster.ApplicationClusterEvent;
import org.springtribe.framework.cluster.HealthState;

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
