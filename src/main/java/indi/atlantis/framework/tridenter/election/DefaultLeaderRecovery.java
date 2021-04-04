package indi.atlantis.framework.tridenter.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.Observable;

import indi.atlantis.framework.tridenter.ApplicationClusterContext;
import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.LeaderState;
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
		applicationClusterContext.setLeaderState(LeaderState.DOWN);
		electionObservable.notifyObservers(leaderInfo);
	}

	protected Observable getObservable() {
		return electionObservable;
	}

}
