package org.springtribe.framework.cluster.multicast;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationListener;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;

import com.github.paganini2008.devtools.collection.CaseInsensitiveMap;
import com.github.paganini2008.devtools.collection.MapUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationRegistryCenter
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ApplicationRegistryCenter implements RegistryCenter, ApplicationListener<ApplicationMulticastEvent> {

	private final List<ApplicationInfo> allApplications = new CopyOnWriteArrayList<ApplicationInfo>();
	private final Map<String, List<ApplicationInfo>> applications = new CaseInsensitiveMap<List<ApplicationInfo>>(
			new ConcurrentHashMap<String, List<ApplicationInfo>>());

	@Override
	public void registerApplication(ApplicationInfo applicationInfo) {
		for (int i = 0; i < applicationInfo.getWeight(); i++) {
			allApplications.add(applicationInfo);
		}
		if (allApplications.size() > 1) {
			Collections.sort(allApplications);
		}

		List<ApplicationInfo> infoList = MapUtils.get(applications, applicationInfo.getApplicationName(), () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		for (int i = 0; i < applicationInfo.getWeight(); i++) {
			infoList.add(applicationInfo);
		}

		if (infoList.size() > 1) {
			Collections.sort(infoList);
		}
		log.info("Register application: [{}] to ApplicationRegistryCenter", applicationInfo);
	}

	@Override
	public void removeApplication(ApplicationInfo applicationInfo) {
		while (allApplications.contains(applicationInfo)) {
			allApplications.remove(applicationInfo);
		}
		List<ApplicationInfo> infoList = applications.get(applicationInfo.getApplicationName());
		if (infoList != null) {
			while (infoList.contains(applicationInfo)) {
				infoList.remove(applicationInfo);
			}
		}
		log.info("Remove application: [{}] from ApplicationRegistryCenter", applicationInfo);
	}

	@Override
	public List<ApplicationInfo> getApplications() {
		return allApplications;
	}

	@Override
	public List<ApplicationInfo> getApplications(String applicationName) {
		return applications.get(applicationName);
	}

	@Override
	public int countOfApplication() {
		return new HashSet<ApplicationInfo>(allApplications).size();
	}

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent event) {
		ApplicationInfo applicationInfo = event.getApplicationInfo();
		if (event.getMulticastEventType() == MulticastEventType.ON_ACTIVE) {
			registerApplication(applicationInfo);
		} else if (event.getMulticastEventType() == MulticastEventType.ON_INACTIVE) {
			removeApplication(applicationInfo);
		}
	}

}
