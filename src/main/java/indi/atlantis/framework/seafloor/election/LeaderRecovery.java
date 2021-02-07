package indi.atlantis.framework.seafloor.election;

import indi.atlantis.framework.seafloor.ApplicationInfo;

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
