package org.springtribe.framework.cluster.election;

import org.springtribe.framework.cluster.ApplicationInfo;

/**
 * 
 * LeaderRecovery
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface LeaderRecovery {

	void recover(ApplicationInfo formerLeader);

}
