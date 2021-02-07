package indi.atlantis.framework.seafloor.multicast;

import static indi.atlantis.framework.seafloor.Constants.APPLICATION_CLUSTER_NAMESPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springtribe.framework.reditools.messager.RedisMessageSender;

import indi.atlantis.framework.seafloor.ApplicationInfo;
import indi.atlantis.framework.seafloor.InstanceId;

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
