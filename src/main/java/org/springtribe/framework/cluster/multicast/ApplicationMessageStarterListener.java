package org.springtribe.framework.cluster.multicast;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.InstanceId;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastEvent.MulticastEventType;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup.MulticastMessage;
import org.springtribe.framework.reditools.messager.RedisMessageHandler;

/**
 * 
 * ApplicationMessageStarterListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class ApplicationMessageStarterListener implements RedisMessageHandler, ApplicationContextAware {

	@Autowired
	private InstanceId instanceId;

	private ApplicationContext applicationContext;

	@Override
	public void onMessage(String channel, Object object) {
		MulticastMessage messageObject = (MulticastMessage) object;
		ApplicationInfo applicationInfo = messageObject.getApplicationInfo();
		ApplicationMulticastEvent multicastEvent = new ApplicationMulticastEvent(applicationContext, applicationInfo,
				MulticastEventType.ON_MESSAGE);
		multicastEvent.setMessage(messageObject);
		applicationContext.publishEvent(multicastEvent);
	}

	@Override
	public String getChannel() {
		return instanceId.get();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
