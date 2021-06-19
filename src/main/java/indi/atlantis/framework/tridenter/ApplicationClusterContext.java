/**
* Copyright 2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import indi.atlantis.framework.tridenter.election.ApplicationClusterRefreshedEvent;

/**
 * 
 * ApplicationClusterContext
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationClusterContext implements SmartApplicationListener {

	private ApplicationInfo leaderInfo;
	private volatile LeaderState leaderState = LeaderState.DOWN;

	public ApplicationInfo getLeaderInfo() {
		return leaderInfo;
	}

	public LeaderState getLeaderState() {
		return leaderState;
	}

	public void setLeaderState(LeaderState leaderState) {
		this.leaderState = leaderState;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		ApplicationClusterEvent applicationClusterEvent = (ApplicationClusterEvent) event;
		this.leaderState = applicationClusterEvent.getLeaderState();

		if (event instanceof ApplicationClusterRefreshedEvent) {
			this.leaderInfo = ((ApplicationClusterRefreshedEvent) event).getLeaderInfo();
		}
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ApplicationClusterRefreshedEvent.class || eventType == ApplicationClusterFatalEvent.class;
	}

}
