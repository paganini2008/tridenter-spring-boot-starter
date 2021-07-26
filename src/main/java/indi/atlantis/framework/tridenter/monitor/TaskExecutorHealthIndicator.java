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
package indi.atlantis.framework.tridenter.monitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.paganini2008.devtools.primitives.Floats;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * TaskExecutorHealthIndicator
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class TaskExecutorHealthIndicator extends AbstractHealthIndicator {

	private static final float WARNING_THRESHOLD = 0.8F;

	@Qualifier("applicationClusterTaskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Override
	protected void doHealthCheck(Builder builder) throws Exception {
		ThreadPoolExecutor executor = taskExecutor.getThreadPoolExecutor();
		int poolSize = executor.getPoolSize();
		int maxPoolSize = executor.getMaximumPoolSize();
		int activeCount = executor.getActiveCount();
		long taskCount = executor.getTaskCount();
		long completedTaskCount = executor.getCompletedTaskCount();
		float ratio;
		if ((ratio = (float) (activeCount / maxPoolSize)) > WARNING_THRESHOLD) {
			log.warn(
					"Available thread count will reach threshold: {}. Current pool size is {}. System need to wait for current active threads to release, otherwise thread pool will block more invocation.",
					Floats.toFixed(ratio, 2), poolSize);
			builder.down();
		} else {
			if (log.isTraceEnabled()) {
				log.trace("TaskExecutor is available now. PoolSize: {}, MaxPoolSize: {}, ActiveCount: {}", poolSize, maxPoolSize,
						activeCount);
			}
			builder.up();
		}

		Map<String, Object> metrics = new LinkedHashMap<String, Object>();
		metrics.put("poolSize", poolSize);
		metrics.put("maxPoolSize", maxPoolSize);
		metrics.put("activeCount", activeCount);
		metrics.put("taskCount", taskCount);
		metrics.put("completedTaskCount", completedTaskCount);
		builder.withDetails(metrics);
	}

}
