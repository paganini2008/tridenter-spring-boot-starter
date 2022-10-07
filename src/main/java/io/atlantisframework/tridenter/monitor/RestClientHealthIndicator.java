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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.github.paganini2008.devtools.collection.MapUtils;

import io.atlantisframework.tridenter.ApplicationClusterContext;
import io.atlantisframework.tridenter.LeaderState;
import io.atlantisframework.tridenter.http.Statistic;
import io.atlantisframework.tridenter.http.StatisticIndicator;

/**
 * 
 * RestClientHealthIndicator
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public class RestClientHealthIndicator extends AbstractHealthIndicator {

	@Qualifier("requestStatistic")
	@Autowired
	private StatisticIndicator requestStatistic;

	@Qualifier("responseStatistic")
	@Autowired
	private StatisticIndicator responseStatistic;

	@Autowired
	private ApplicationClusterContext applicationClusterContext;

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		LeaderState leaderState = applicationClusterContext.getLeaderState();
		if (leaderState == LeaderState.FATAL) {
			builder.down();
		} else {
			builder.up();
		}
		Map<String, Collection<Statistic>> source = requestStatistic.toEntries();
		if (MapUtils.isNotEmpty(source)) {
			for (Map.Entry<String, Collection<Statistic>> entry : source.entrySet()) {
				builder.withDetail("[-]: " + entry.getKey(),
						entry.getValue().stream().map(stat -> stat.toEntries()).collect(Collectors.toList()));
			}
		}
		source = responseStatistic.toEntries();
		if (MapUtils.isNotEmpty(source)) {
			for (Map.Entry<String, Collection<Statistic>> entry : source.entrySet()) {
				builder.withDetail("[+]: " + entry.getKey(),
						entry.getValue().stream().map(stat -> stat.toEntries()).collect(Collectors.toList()));
			}
		}

	}

}
