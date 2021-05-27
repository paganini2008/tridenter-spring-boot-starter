package indi.atlantis.framework.tridenter.consistency;

import org.springframework.beans.factory.annotation.Autowired;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestPreparationRequest
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestPreparationRequest implements ApplicationMessageListener {

	@Autowired
	private ConsistencyRequestRound requestRound;

	@Autowired
	private ConsistencyRequestSerialCache requestSerialCache;

	@Autowired
	private InstanceId instanceId;

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
			log.trace(getTopic() + " " + anotherInstanceId + ", " + message);
		}
		long round = request.getRound();
		long serial = request.getSerial();
		long maxSerial = requestSerialCache.getSerial(name, round);
		if (serial > maxSerial) {
			Object preValue = requestSerialCache.setValue(name, round, serial, request.getValue());
			if (preValue != null) {
				System.out.println("preValue: " + preValue);
				request.setValue(preValue);
			}
			multicastGroup.send(anotherInstanceId, ConsistencyRequest.PREPARATION_OPERATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), true));
		} else {
			multicastGroup.send(anotherInstanceId, ConsistencyRequest.PREPARATION_OPERATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), false));
		}
	}

	@Override
	public String getTopic() {
		return ConsistencyRequest.PREPARATION_OPERATION_REQUEST;
	}

}
