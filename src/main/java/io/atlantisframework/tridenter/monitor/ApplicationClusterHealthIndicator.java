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
package io.atlantisframework.tridenter.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.github.paganini2008.devtools.io.FileUtils;

import io.atlantisframework.tridenter.ApplicationClusterContext;
import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.LeaderState;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastGroup;

/**
 * 
 * ApplicationClusterHealthIndicator
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public class ApplicationClusterHealthIndicator extends AbstractHealthIndicator {

	@Autowired
	private ApplicationClusterContext applicationClusterContext;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		LeaderState leaderState = applicationClusterContext.getLeaderState();
		if (leaderState == LeaderState.FATAL) {
			builder.down();
		} else {
			builder.up();
		}
		builder.withDetail("leaderState", leaderState);
		builder.withDetail("candidates", applicationMulticastGroup.countOfCandidate());
		builder.withDetail("leader", getLeaderInfo());
		builder.withDetail("totalMemory", FileUtils.formatSize(Runtime.getRuntime().totalMemory()));
		builder.withDetail("usedMemory", FileUtils.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
	}

	private ApplicationInfo getLeaderInfo() {
		for (ApplicationInfo candidate : applicationMulticastGroup.getCandidates()) {
			if (candidate.isLeader()) {
				return candidate;
			}
		}
		return null;
	}

}
