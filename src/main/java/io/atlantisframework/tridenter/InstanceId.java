/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.io.IOUtils;
import com.github.paganini2008.devtools.net.NetUtils;

import io.atlantisframework.tridenter.utils.Contact;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * InstanceId
 * 
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public final class InstanceId {

	private final long startTime = System.currentTimeMillis();

	@Autowired
	private InstanceIdGenerator idGenerator;
	
	@Autowired
	private Contact contact;

	@Getter
	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Getter
	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${server.port}")
	private int port;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	@Value("${spring.application.cluster.id:}")
	private String id;

	@Getter
	@Value("${spring.application.cluster.weight:1}")
	private int weight;

	@Getter
	@Value("${spring.application.cluster.applicationContextPath:}")
	private String applicationContextPath;

	@Setter
	@Getter
	private volatile @Nullable ApplicationInfo leaderInfo;

	public String get() {
		if (StringUtils.isBlank(id)) {
			synchronized (this) {
				if (StringUtils.isBlank(id)) {
					id = idGenerator.generateInstanceId();
					log.info(IOUtils.NEWLINE + "\tGenerate the instanceId: " + id);
				}
			}
		}
		return id;
	}

	public boolean isLeader() {
		if (leaderInfo == null) {
			return false;
		}
		return get().equals(leaderInfo.getId());
	}

	public long getStartTime() {
		return startTime;
	}

	public ApplicationInfo getApplicationInfo() {
		ApplicationInfo applicationInfo = new ApplicationInfo(get(), clusterName, applicationName, getLeaderInfo());
		applicationInfo.setWeight(getWeight());
		applicationInfo.setStartTime(getStartTime());
		applicationInfo.setContact(contact);
		String applicationContextPath = this.applicationContextPath;
		if (StringUtils.isBlank(applicationContextPath)) {
			applicationContextPath = "http://" + NetUtils.getLocalHost() + ":" + port + contextPath;
		}
		applicationInfo.setApplicationContextPath(applicationContextPath);
		return applicationInfo;
	}

	public String toString() {
		return "InstanceId: " + get() + ", Leader: " + isLeader();
	}

}
