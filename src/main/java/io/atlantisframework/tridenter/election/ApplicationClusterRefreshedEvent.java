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
package io.atlantisframework.tridenter.election;

import org.springframework.context.ApplicationContext;

import io.atlantisframework.tridenter.ApplicationClusterEvent;
import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.LeaderState;

/**
 * 
 * ApplicationClusterRefreshedEvent
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class ApplicationClusterRefreshedEvent extends ApplicationClusterEvent {

	private static final long serialVersionUID = 3115067071903624457L;

	public ApplicationClusterRefreshedEvent(ApplicationContext applicationContext, ApplicationInfo leader) {
		super(applicationContext, LeaderState.UP);
		this.leader = leader;
	}

	private final ApplicationInfo leader;

	public ApplicationInfo getLeaderInfo() {
		return leader;
	}

}
