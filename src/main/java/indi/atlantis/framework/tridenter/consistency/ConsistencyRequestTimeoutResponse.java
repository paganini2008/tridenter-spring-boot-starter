package indi.atlantis.framework.tridenter.consistency;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestTimeoutResponse
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestTimeoutResponse implements ApplicationMessageListener, ApplicationContextAware {

	@Autowired
	private ConsistencyRequestRound requestRound;

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
		applicationContext.publishEvent(new ConsistencyRequestCompletionEvent(request, applicationInfo));
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.TIMEOUT_OPERATION_RESPONSE;
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
