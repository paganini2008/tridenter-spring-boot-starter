package indi.atlantis.framework.seafloor.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import indi.atlantis.framework.seafloor.ApplicationInfo;
import indi.atlantis.framework.seafloor.multicast.ApplicationMessageListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestPreparationResponse
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestPreparationResponse implements ApplicationMessageListener {

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
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + applicationInfo.getId() + ", " + response);
		}
		if (response.isAcceptable()) {
			court.canCommit(response);
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.PREPARATION_OPERATION_RESPONSE;
	}

}
