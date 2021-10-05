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
package io.atlantisframework.tridenter.multicast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.multithreads.Executable;
import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.InstanceId;
import io.atlantisframework.tridenter.LoadBalancer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationMulticastGroup
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class ApplicationMulticastGroup {

	private final List<ApplicationInfo> allCandidates = new CopyOnWriteArrayList<ApplicationInfo>();
	private final Map<String, List<ApplicationInfo>> groupCandidates = new ConcurrentHashMap<String, List<ApplicationInfo>>();
	private final Map<String, ApplicationInfo> offlineCandidates = new ConcurrentHashMap<String, ApplicationInfo>();

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Qualifier("applicationMulticastLoadBalancer")
	@Autowired
	private LoadBalancer loadBalancer;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private MulticastMessageAcker multicastMessageAcker;

	private MulticastMessageAckerChecker multicastMessageAckerChecker;

	@PostConstruct
	public void configure() {
		multicastMessageAckerChecker = new MulticastMessageAckerChecker();
		multicastMessageAckerChecker.start();
	}

	public void registerCandidate(ApplicationInfo applicationInfo) {
		for (int i = 0; i < applicationInfo.getWeight(); i++) {
			allCandidates.add(applicationInfo);
		}
		if (allCandidates.size() > 1) {
			Collections.sort(allCandidates);
		}

		List<ApplicationInfo> candidates = MapUtils.get(groupCandidates, applicationInfo.getApplicationName(), () -> {
			return new CopyOnWriteArrayList<ApplicationInfo>();
		});
		for (int i = 0; i < applicationInfo.getWeight(); i++) {
			candidates.add(applicationInfo);
		}
		if (candidates.size() > 1) {
			Collections.sort(candidates);
		}
		log.info("Registered candidate: {}, Proportion: {}/{}", applicationInfo, candidates.size(), allCandidates.size());
	}

	public boolean hasRegistered(ApplicationInfo applicationInfo) {
		return groupCandidates.containsKey(applicationInfo.getApplicationName())
				? groupCandidates.get(applicationInfo.getApplicationName()).contains(applicationInfo)
				: false;
	}

	public void removeCandidate(ApplicationInfo applicationInfo) {
		while (allCandidates.contains(applicationInfo)) {
			allCandidates.remove(applicationInfo);
		}

		List<ApplicationInfo> candidates = groupCandidates.get(applicationInfo.getApplicationName());
		if (candidates != null) {
			while (candidates.contains(applicationInfo)) {
				candidates.remove(applicationInfo);
			}
		}
		log.info("Removed candidate: {}, Proportion: {}/{}", applicationInfo, candidates.size(), allCandidates.size());

	}

	public void offline(String appId) {
		ApplicationInfo matched = null;
		for (ApplicationInfo applicationInfo : getCandidates()) {
			if (applicationInfo.getId().equals(appId)) {
				matched = applicationInfo;
				break;
			}
		}
		if (matched != null) {
			removeCandidate(matched);
			offlineCandidates.put(appId, matched);
		}
	}

	public void online(String appId) {
		ApplicationInfo applicationInfo = offlineCandidates.get(appId);
		if (applicationInfo != null) {
			registerCandidate(applicationInfo);
		}
	}

	public int countOfCandidate() {
		return getCandidates().length;
	}

	public int countOfCandidate(String group) {
		return getCandidates(group).length;
	}

	public ApplicationInfo[] getCandidates() {
		return new TreeSet<ApplicationInfo>(allCandidates).toArray(new ApplicationInfo[0]);
	}

	public ApplicationInfo[] getCandidates(String group) {
		if (groupCandidates.containsKey(group)) {
			return new TreeSet<ApplicationInfo>(groupCandidates.get(group)).toArray(new ApplicationInfo[0]);
		}
		return new ApplicationInfo[0];
	}

	public ApplicationInfo[] getOfflineCandidates() {
		List<ApplicationInfo> candidates = new ArrayList<ApplicationInfo>(offlineCandidates.values());
		Function<ApplicationInfo, String> function = info -> info.getClusterName();
		candidates = candidates.stream().sorted(Comparator.comparing(function).thenComparing(ApplicationInfo::getApplicationName))
				.collect(Collectors.toList());
		return candidates.toArray(new ApplicationInfo[0]);
	}

	public void unicast(String topic, Object message) {
		unicast(topic, message, -1);
	}

	public void unicast(String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		ApplicationInfo applicationInfo = loadBalancer.select(null, allCandidates, message);
		if (applicationInfo != null) {
			doSendMessage(applicationInfo.getId(), topic, message, timeout);
		}
	}

	public void unicast(String group, String topic, Object message) {
		unicast(group, topic, message, -1);
	}

	public void unicast(String group, String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		if (groupCandidates.containsKey(group)) {
			ApplicationInfo applicationInfo = loadBalancer.select(group, groupCandidates.get(group), message);
			if (applicationInfo != null) {
				doSendMessage(applicationInfo.getId(), topic, message, timeout);
			}
		}
	}

	public void multicast(String topic, Object message) {
		multicast(topic, message, -1);
	}

	public void multicast(String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		for (ApplicationInfo applicationInfo : getCandidates()) {
			doSendMessage(applicationInfo.getId(), topic, message, timeout);
		}
	}

	public void multicast(String group, String topic, Object message) {
		multicast(group, topic, message, -1);
	}

	public void multicast(String group, String topic, Object message, int timeout) {
		Assert.hasNoText(topic, "Topic must be required");
		for (ApplicationInfo applicationInfo : getCandidates(group)) {
			doSendMessage(applicationInfo.getId(), topic, message, timeout);
		}
	}

	public void send(String channel, String topic, Object message) {
		send(channel, topic, message, -1);
	}

	public void send(String channel, String topic, Object message, int timeout) {
		Assert.hasNoText(channel, "Channel must be required");
		Assert.hasNoText(topic, "Topic must be required");
		doSendMessage(channel, topic, message, timeout);
	}

	public void ack(String channel, MulticastMessage messageObject) {
		messageObject.setTopic(MulticastMessageAcker.DEFAULT_TOPIC_NAME);
		doSendMessage(channel, messageObject, -1);
	}

	private void doSendMessage(String channel, String topic, Object message, int timeout) {
		MulticastMessage messageObject = createMessageObject(channel, topic, message, timeout);
		doSendMessage(channel, messageObject, timeout);
	}

	void doSendMessage(String channel, MulticastMessage messageObject, int timeout) {
		redisMessageSender.sendMessage(channel, messageObject);
		if (timeout > 0) {
			multicastMessageAcker.waitForAck(messageObject);
		}
	}

	protected MulticastMessage createMessageObject(String channel, String topic, Object message, int timeout) {
		MulticastMessage messageObject = new MulticastMessage();
		messageObject.setApplicationInfo(instanceId.getApplicationInfo());
		messageObject.setChannel(channel);
		messageObject.setTopic(topic);
		messageObject.setMessage(message);
		messageObject.setTimeout(timeout);
		return messageObject;
	}

	private class MulticastMessageAckerChecker implements Executable {

		private Timer timer;

		public void start() {
			timer = ThreadUtils.scheduleWithFixedDelay(this, 5, 5, TimeUnit.SECONDS);
		}

		@Override
		public boolean execute() {
			multicastMessageAcker.retrySendMessage(ApplicationMulticastGroup.this);
			return true;
		}

		public void close() {
			if (timer != null) {
				timer.cancel();
			}
		}

	}

	/**
	 * 
	 * MulticastMessage
	 * 
	 * @author Fred Feng
	 *
	 * @since 2.0.1
	 */
	@Getter
	@Setter
	public static class MulticastMessage implements Serializable {

		private static final long serialVersionUID = 1L;

		public MulticastMessage() {
			this.id = UUID.randomUUID().toString();
			this.timestamp = System.currentTimeMillis();
		}

		private String id;
		private long timestamp;
		private ApplicationInfo applicationInfo;
		private String channel;
		private String topic;
		private Object message;
		private int timeout;

		@Override
		public int hashCode() {
			final int prime = 37;
			return prime + prime * id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof MulticastMessage) {
				MulticastMessage message = (MulticastMessage) obj;
				return message.getId().equals(getId());
			}
			return false;
		}

	}

	@PreDestroy
	public void close() {
		multicastMessageAckerChecker.close();
	}

}
