package indi.atlantis.framework.tridenter.monitor;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.github.paganini2008.devtools.collection.MapUtils;

import indi.atlantis.framework.tridenter.ApplicationClusterContext;
import indi.atlantis.framework.tridenter.LeaderState;
import indi.atlantis.framework.tridenter.http.Statistic;
import indi.atlantis.framework.tridenter.http.StatisticIndicator;

/**
 * 
 * RestClientHealthIndicator
 *
 * @author Jimmy Hoff
 * @version 1.0
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
