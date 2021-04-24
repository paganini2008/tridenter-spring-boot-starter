package indi.atlantis.framework.tridenter.pool;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;

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
