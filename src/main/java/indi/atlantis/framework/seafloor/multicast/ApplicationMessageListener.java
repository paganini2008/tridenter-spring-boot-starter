package indi.atlantis.framework.seafloor.multicast;

import indi.atlantis.framework.seafloor.ApplicationInfo;

/**
 * 
 * ApplicationMessageListener
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface ApplicationMessageListener extends ApplicationClusterListener {

	void onMessage(ApplicationInfo applicationInfo, String id, Object message);

	default String getTopic() {
		return "*";
	}

}
