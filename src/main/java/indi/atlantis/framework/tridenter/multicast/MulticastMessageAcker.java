package indi.atlantis.framework.tridenter.multicast;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup.MulticastMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MulticastMessageAcker
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class MulticastMessageAcker implements ApplicationMessageListener {

	static final String DEFAULT_TOPIC_NAME = "<MULTICAST-MESSAGE-ACKER>";

	private final Map<String, MulticastMessage> ackQueue = new ConcurrentHashMap<String, MulticastMessage>();

	public void waitForAck(MulticastMessage message) {
		ackQueue.put(message.getId(), message);
	}

	public void retrySendMessage(ApplicationMulticastGroup multicastGroup) {
		if (ackQueue.isEmpty()) {
			return;
		}
		final Queue<MulticastMessage> q = new ArrayDeque<MulticastMessage>(ackQueue.values());
		while (!q.isEmpty()) {
			MulticastMessage message = q.poll();
			if (System.currentTimeMillis() - message.getTimestamp() > message.getTimeout() * 1000) {
				ackQueue.remove(message.getId());
				multicastGroup.doSendMessage(message.getChannel(), message, message.getTimeout());
				if (log.isTraceEnabled()) {
					log.trace("Resend MulticastMessage '{}'", message.getId());
				}
			}
		}
	}

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object obj) {
		if (ackQueue.remove(id) != null) {
			if (log.isTraceEnabled()) {
				log.trace("Acknowledge clusterMulticastMessage '{}'", id);
			}
		}
	}

	@Override
	public String getTopic() {
		return DEFAULT_TOPIC_NAME;
	}

}
