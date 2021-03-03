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
 * @author Jimmy Hoff
 * @version 1.0
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
