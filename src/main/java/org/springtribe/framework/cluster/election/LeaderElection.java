package org.springtribe.framework.cluster.election;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * LeaderElection
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface LeaderElection {

	static final int DEFAULT_TIMEOUT = 60;

	void launch();

	void onTriggered(ApplicationEvent applicationEvent);

}
