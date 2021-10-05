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
package io.atlantisframework.tridenter.multiprocess;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.springdessert.reditools.common.ProcessLock;
import com.github.paganini2008.springdessert.reditools.common.RedisTtlKeeper;

import io.atlantisframework.tridenter.LeaderState;
import io.atlantisframework.tridenter.election.ApplicationClusterLeaderEvent;

/**
 * 
 * ProcessLockTtlKeeper
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
public class ProcessLockTtlKeeper implements ApplicationListener<ApplicationClusterLeaderEvent> {

	@Autowired
	private RedisTtlKeeper redisTtlKeeper;

	@Override
	public void onApplicationEvent(ApplicationClusterLeaderEvent event) {
		if (event.getLeaderState() == LeaderState.UP) {
			Map<String, ProcessLock> map = event.getApplicationContext().getBeansOfType(ProcessLock.class);
			for (ProcessLock lock : map.values()) {
				redisTtlKeeper.watchKey(lock.getLockName(), lock.getExpiration(), TimeUnit.SECONDS);
			}
		}
	}

}
