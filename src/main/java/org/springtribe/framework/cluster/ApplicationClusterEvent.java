package org.springtribe.framework.cluster;

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

	public ApplicationClusterEvent(ApplicationContext applicationContext, HealthState healthState) {
		super(applicationContext);
		this.healthState = healthState;
	}

	private final HealthState healthState;

	public HealthState getHealthState() {
		return healthState;
	}

}
