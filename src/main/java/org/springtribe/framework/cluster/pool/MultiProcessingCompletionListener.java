package org.springtribe.framework.cluster.pool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.multicast.ApplicationMessageListener;
import org.springtribe.framework.reditools.messager.RedisMessageSender;

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
