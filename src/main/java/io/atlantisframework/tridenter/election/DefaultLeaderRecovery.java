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
package io.atlantisframework.tridenter.election;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.Observable;

import io.atlantisframework.tridenter.ApplicationClusterContext;
import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.LeaderState;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultLeaderRecovery
 *
 * @author Fred Feng
 * @since 2.0.1
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
