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
package io.atlantisframework.tridenter.multicast;

import io.atlantisframework.tridenter.ApplicationInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LoggingApplicationMulticastListener
 *
 * @author Fred Feng
 * @since 2.0.1
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
