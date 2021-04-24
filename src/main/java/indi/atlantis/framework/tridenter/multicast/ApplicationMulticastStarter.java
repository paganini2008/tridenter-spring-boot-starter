package indi.atlantis.framework.tridenter.multicast;

import static indi.atlantis.framework.tridenter.Constants.APPLICATION_CLUSTER_NAMESPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.springworld.reditools.messager.RedisMessageSender;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;

/**
 * 
 * ApplicationMulticastStarter
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ApplicationMulticastStarter implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		final String channel = APPLICATION_CLUSTER_NAMESPACE + clusterName + ":active";
		redisMessageSender.sendMessage(channel, applicationInfo);
	}

}
