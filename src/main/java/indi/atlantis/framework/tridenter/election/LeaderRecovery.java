package indi.atlantis.framework.tridenter.election;

import indi.atlantis.framework.tridenter.ApplicationInfo;

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
