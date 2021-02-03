package org.springtribe.framework.cluster.multicast;

import org.springtribe.framework.cluster.ApplicationInfo;

/**
 * 
 * ApplicationMulticastListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface ApplicationMulticastListener extends ApplicationClusterListener {

	default void onActive(ApplicationInfo applicationInfo) {
	}

	default void onInactive(ApplicationInfo applicationInfo) {
	}

	default void onGlobalMessage(ApplicationInfo applicationInfo, String id, Object message) {
	}

}
