package org.springtribe.framework.cluster;

import org.springframework.context.ApplicationContext;

/**
 * 
 * ApplicationClusterFatalEvent
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ApplicationClusterFatalEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -922319097605054253L;

	public ApplicationClusterFatalEvent(ApplicationContext source, Throwable reason) {
		super(source, HealthState.FATAL);
		this.reason = reason;
	}

	private final Throwable reason;

	public Throwable getReason() {
		return reason;
	}

}
