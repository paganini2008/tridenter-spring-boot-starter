package org.springtribe.framework.cluster.election;

import org.springframework.context.ApplicationContext;
import org.springtribe.framework.cluster.ApplicationClusterEvent;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.HealthState;

/**
 * 
 * ApplicationClusterRefreshedEvent
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ApplicationClusterRefreshedEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = 3115067071903624457L;

	public ApplicationClusterRefreshedEvent(ApplicationContext applicationContext, ApplicationInfo leader) {
		super(applicationContext, HealthState.LEADABLE);
		this.leader = leader;
	}

	private final ApplicationInfo leader;

	public ApplicationInfo getLeaderInfo() {
		return leader;
	}

}
