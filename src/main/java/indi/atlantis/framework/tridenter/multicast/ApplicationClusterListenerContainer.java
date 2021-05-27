package indi.atlantis.framework.tridenter.multicast;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.MapUtils;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup.MulticastMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterListenerContainer
 * 
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
public class ApplicationClusterListenerContainer implements BeanPostProcessor, ApplicationListener<ApplicationMulticastEvent> {

	private final List<ApplicationMulticastListener> listeners = new CopyOnWriteArrayList<ApplicationMulticastListener>();
	private final Map<String, List<ApplicationMessageListener>> messageListeners = new ConcurrentHashMap<String, List<ApplicationMessageListener>>();

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent event) {
		ApplicationInfo applicationInfo = event.getApplicationInfo();
		MulticastEventType eventType = event.getMulticastEventType();
		switch (eventType) {
		case ON_ACTIVE:
			fireOnActive(applicationInfo);
			break;
		case ON_INACTIVE:
			fireOnInactive(applicationInfo);
			break;
		case ON_MESSAGE:
			doFireOnMessage(applicationInfo, (MulticastMessage) event.getMessage());
			break;
		}
	}

	public void fireOnActive(final ApplicationInfo applicationInfo) {
		List<ApplicationMulticastListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onActive(applicationInfo);
			});
		}
	}

	public void fireOnInactive(final ApplicationInfo applicationInfo) {
		List<ApplicationMulticastListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onInactive(applicationInfo);
			});
		}
	}

	private void doFireOnMessage(ApplicationInfo applicationInfo, MulticastMessage messageObject) {
		String id = messageObject.getId();
		String topic = messageObject.getTopic();
		Object message = messageObject.getMessage();
		boolean ack = messageObject.getTimeout() > 0;
		try {
			if (StringUtils.isNotBlank(topic)) {
				fireOnMessage(applicationInfo, topic, id, message);
				if (ack) {
					applicationMulticastGroup.ack(applicationInfo.getId(), messageObject);
				}
			} else {
				fireOnMessage(applicationInfo, id, message);
			}
		} catch (Throwable e) {
			log.error("Failed to send MulticastMessage '{}'", id, e);
		}
	}

	public void fireOnMessage(final ApplicationInfo applicationInfo, final String id, final Object message) {
		List<ApplicationMulticastListener> eventListeners = listeners;
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onGlobalMessage(applicationInfo, id, message);
			});
		}
	}

	public void fireOnMessage(final ApplicationInfo applicationInfo, final String topic, final String id, final Object message) {
		List<ApplicationMessageListener> eventListeners = messageListeners.get(topic);
		if (eventListeners != null) {
			eventListeners.forEach(handler -> {
				handler.onMessage(applicationInfo, id, message);
			});
		}
	}

	public void registerListener(ApplicationMessageListener eventListener) {
		final String topic = eventListener.getTopic();
		List<ApplicationMessageListener> eventListeners = MapUtils.get(messageListeners, topic, () -> {
			return new CopyOnWriteArrayList<ApplicationMessageListener>();
		});
		if (!eventListeners.contains(eventListener)) {
			eventListeners.add(eventListener);
		}
	}

	public void unregisterListener(ApplicationMessageListener eventListener) {
		final String topic = eventListener.getTopic();
		List<ApplicationMessageListener> eventListeners = messageListeners.get(topic);
		if (eventListeners != null && eventListeners.contains(eventListener)) {
			eventListeners.remove(eventListener);
			if (eventListeners.isEmpty()) {
				messageListeners.remove(topic);
			}
		}
	}

	public void registerListener(ApplicationMulticastListener eventListener) {
		if (!listeners.contains(eventListener)) {
			listeners.add(eventListener);
		}
	}

	public void unregisterListener(ApplicationMulticastListener eventListener) {
		if (listeners.contains(eventListener)) {
			listeners.remove(eventListener);
		}
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ApplicationMulticastListener) {
			registerListener((ApplicationMulticastListener) bean);
		} else if (bean instanceof ApplicationMessageListener) {
			registerListener((ApplicationMessageListener) bean);
		}
		return bean;
	}

}
