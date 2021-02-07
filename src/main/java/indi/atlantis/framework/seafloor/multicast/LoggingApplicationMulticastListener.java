package indi.atlantis.framework.seafloor.multicast;

import indi.atlantis.framework.seafloor.ApplicationInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LoggingApplicationMulticastListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class LoggingApplicationMulticastListener implements ApplicationMulticastListener, ApplicationMessageListener {

	@Override
	public void onActive(ApplicationInfo applicationInfo) {
		if (log.isTraceEnabled()) {
			log.trace("Application '{}' has joined.", applicationInfo);
		}
	}

	@Override
	public void onInactive(ApplicationInfo applicationInfo) {
		if (log.isTraceEnabled()) {
			log.trace("Application '{}' has gone.", applicationInfo);
		}
	}

	@Override
	public void onGlobalMessage(ApplicationInfo applicationInfo, String id, Object message) {
		if (log.isTraceEnabled()) {
			log.trace("Application '{}' send global message: {}", applicationInfo.getId(), message);
		}
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		if (log.isTraceEnabled()) {
			log.trace("Application '{}' send message: {}", applicationInfo.getId(), message);
		}
	}

}
