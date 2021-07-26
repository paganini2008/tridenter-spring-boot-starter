/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

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

import org.springframework.context.ApplicationContext;

import indi.atlantis.framework.tridenter.ApplicationClusterEvent;
import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.LeaderState;

/**
 * 
 * ApplicationMulticastEvent
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public class ApplicationMulticastEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = -2482108960259276628L;

	public ApplicationMulticastEvent(ApplicationContext source, ApplicationInfo applicationInfo, MulticastEventType eventType) {
		super(source, LeaderState.DOWN);
		this.applicationInfo = applicationInfo;
		this.multicastEventType = eventType;
	}

	private final ApplicationInfo applicationInfo;
	private final MulticastEventType multicastEventType;
	private Object message;

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public ApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	public MulticastEventType getMulticastEventType() {
		return multicastEventType;
	}

	public static enum MulticastEventType {
		ON_ACTIVE, ON_INACTIVE, ON_MESSAGE;
	}

}
