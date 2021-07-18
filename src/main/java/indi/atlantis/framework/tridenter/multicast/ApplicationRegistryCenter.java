/**
* Copyright 2018-2021 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package indi.atlantis.framework.tridenter.multicast;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.collection.CaseInsensitiveMap;
import com.github.paganini2008.devtools.collection.MapUtils;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationRegistryCenter
 *
 * @author Fred Feng
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
