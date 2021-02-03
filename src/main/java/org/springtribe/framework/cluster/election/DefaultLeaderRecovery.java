package org.springtribe.framework.cluster.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springtribe.framework.cluster.ApplicationClusterContext;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.HealthState;

import com.github.paganini2008.devtools.Observable;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultLeaderRecovery
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class DefaultLeaderRecovery implements ApplicationListener<ApplicationClusterFollowerEvent>, LeaderRecovery {

	private final Observable electionObservable = Observable.unrepeatable();

	@Autowired
	private LeaderElection leaderElection;

	@Autowired
	protected ApplicationClusterContext applicationClusterContext;

	@Override
	public void onApplicationEvent(ApplicationClusterFollowerEvent event) {
		electionObservable.addObserver((ob, arg) -> {
			log.info("Launch new round leader election soon");
			leaderElection.launch();
		});
	}

	@Override
	public void recover(ApplicationInfo leaderInfo) {
		applicationClusterContext.setHealthState(HealthState.UNLEADABLE);
		electionObservable.notifyObservers(leaderInfo);
	}

	protected Observable getObservable() {
		return electionObservable;
	}

}
