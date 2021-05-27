package indi.atlantis.framework.tridenter.multicast;

import indi.atlantis.framework.tridenter.ApplicationInfo;

/**
 * 
 * ApplicationMessageListener
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface ApplicationMessageListener extends ApplicationClusterListener {

	void onMessage(ApplicationInfo applicationInfo, String id, Object message);

	default String getTopic() {
		return "*";
	}

}
