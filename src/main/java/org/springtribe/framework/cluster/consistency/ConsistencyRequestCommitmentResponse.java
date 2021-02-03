package org.springtribe.framework.cluster.consistency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.multicast.ApplicationMessageListener;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestCommitmentResponse
 *
 * @author Jimmy Hoff
 * @since 1.0
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
