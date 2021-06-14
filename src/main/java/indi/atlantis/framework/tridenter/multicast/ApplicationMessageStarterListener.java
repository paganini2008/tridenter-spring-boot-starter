package indi.atlantis.framework.tridenter.multicast;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup.MulticastMessage;

/**
 * 
 * ApplicationMessageStarterListener
 *
 * @author Fred Feng
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
