package org.springtribe.framework.cluster.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.HealthState;
import org.springtribe.framework.cluster.election.DefaultLeaderRecovery;
import org.springtribe.framework.cluster.multicast.ApplicationHeartbeatTask;

import com.github.paganini2008.devtools.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RetryableLeaderRecovery
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class RetryableLeaderRecovery extends DefaultLeaderRecovery implements ApiRetryListener {

	@Autowired
	private LeaderService leaderService;

	@Override
	public void recover(ApplicationInfo formerLeader) {
		applicationClusterContext.setHealthState(HealthState.UNLEADABLE);
		try {
			leaderService.ping();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onRetryBegin(String provider, Request request) {
	}

	@Override
	public void onEachRetry(String provider, Request request, Throwable e) {
		ApplicationInfo leader = applicationClusterContext.getLeaderInfo();
		log.warn("Attempt to keep heartbeating from cluster leader [{}]", leader);
	}

	@Override
	public void onRetryEnd(String provider, Request request, Throwable e) {
		ApplicationInfo leader = applicationClusterContext.getLeaderInfo();
		if (applicationClusterContext.getHealthState() == HealthState.UNLEADABLE) {
			log.warn("Application cluster leader [{}] is exhausted", leader);
			super.recover(leader);
		}
	}

	@Override
	public boolean matches(String provider, Request request) {
		return StringUtils.isNotBlank(request.getPath()) && request.getPath().equals(ApplicationHeartbeatTask.APPLICATION_PING_PATH);
	}

}
