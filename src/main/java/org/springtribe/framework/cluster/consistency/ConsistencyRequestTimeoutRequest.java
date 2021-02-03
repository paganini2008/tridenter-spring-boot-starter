package org.springtribe.framework.cluster.consistency;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.multicast.ApplicationMessageListener;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestTimeoutRequest
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestTimeoutRequest implements ApplicationMessageListener, ApplicationContextAware {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final ConsistencyRequest request = (ConsistencyRequest) message;
		final String name = request.getName();
		if (request.getRound() != requestRound.currentRound(name)) {
			if (log.isTraceEnabled()) {
				log.trace("This round of proposal '{}' has been finished.", name);
			}
			return;
		}
		String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + request);
		}
		applicationContext.publishEvent(new ConsistencyRequestConfirmationEvent(request, applicationInfo, false));
		multicastGroup.send(anotherInstanceId, ConsistencyRequest.TIMEOUT_OPERATION_RESPONSE, request);
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.TIMEOUT_OPERATION_REQUEST;
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
