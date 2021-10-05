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
package io.atlantisframework.tridenter.http;

import org.springframework.context.ApplicationListener;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.election.ApplicationClusterFollowerEvent;
import io.atlantisframework.tridenter.election.LeaderNotFoundException;

/**
 * 
 * LeaderRoutingAllocator
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class LeaderRoutingAllocator implements RoutingAllocator, ApplicationListener<ApplicationClusterFollowerEvent> {

	private ApplicationInfo leaderInfo;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		this.leaderInfo = event.getLeaderInfo();
	}

	@Override
	public String allocateHost(String provider, String path, Request request) {
		if (leaderInfo == null) {
			throw new LeaderNotFoundException();
		}
		return leaderInfo.getApplicationContextPath() + path;
	}

}
