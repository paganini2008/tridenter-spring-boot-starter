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
