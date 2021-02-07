package indi.atlantis.framework.seafloor.pool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springtribe.framework.reditools.messager.RedisMessageSender;

import indi.atlantis.framework.seafloor.ApplicationInfo;
import indi.atlantis.framework.seafloor.multicast.ApplicationMessageListener;

/**
 * 
 * MultiProcessingCompletionListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class MultiProcessingCompletionListener implements ApplicationMessageListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		redisMessageSender.sendMessage(((Return) message).getInvocation().getId(), message);
	}

	@Override
	public String getTopic() {
		return MultiProcessingCompletionListener.class.getName();
	}

}
