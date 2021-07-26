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
package indi.atlantis.framework.tridenter.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestCommitmentResponse
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class ConsistencyRequestCommitmentResponse implements ApplicationMessageListener {

	@Autowired
	private Court court;

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final ConsistencyResponse response = (ConsistencyResponse) message;
		final ConsistencyRequest request = response.getRequest();
		final String name = request.getName();
		if (request.getRound() != requestRound.currentRound(name)) {
			if (log.isTraceEnabled()) {
				log.trace("This round of proposal '{}' has been finished.", name);
			}
			return;
		}
		String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + response);
		}
		if (response.isAcceptable()) {
			court.canLearn(response);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.COMMITMENT_OPERATION_RESPONSE;
	}

}
