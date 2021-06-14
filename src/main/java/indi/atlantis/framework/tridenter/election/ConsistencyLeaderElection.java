package indi.atlantis.framework.tridenter.election;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.springdessert.reditools.RedisComponentNames;
import com.github.paganini2008.springdessert.reditools.common.TtlKeeper;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.Constants;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.consistency.ConsistencyRequestConfirmationEvent;
import indi.atlantis.framework.tridenter.consistency.ConsistencyRequestContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyLeaderElection
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyLeaderElection implements LeaderElection, ApplicationContextAware, SmartApplicationListener {

	private ApplicationContext applicationContext;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Value("${spring.application.cluster.leader.lease:10}")
	private int leaderLease;

	@Autowired
	private InstanceId instanceId;

	@Qualifier(RedisComponentNames.REDIS_TEMPLATE)
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private TtlKeeper ttlKeeper;

	@Autowired
	private ConsistencyRequestContext requestContext;

	@Override
	public void launch() {
		final String leaderIdentify = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":leader";
		requestContext.propose(leaderIdentify, instanceId.getApplicationInfo(), DEFAULT_TIMEOUT);
		log.info("Start leader election. Identify: " + leaderIdentify);
	}

	@Override
	public void onTriggered(ApplicationEvent applicationEvent) {
		final ConsistencyRequestConfirmationEvent event = (ConsistencyRequestConfirmationEvent) applicationEvent;
		ApplicationInfo leaderInfo = (ApplicationInfo) event.getRequest().getValue();
		if (instanceId.getApplicationInfo().equals(leaderInfo)) {
			applicationContext.publishEvent(new ApplicationClusterLeaderEvent(applicationContext));
			log.info("This is the leader of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterLeaderEvent.class.getName());
		} else {
			applicationContext.publishEvent(new ApplicationClusterFollowerEvent(applicationContext, leaderInfo));
			log.info("This is the follower of application cluster '{}'. Current application event type is '{}'", clusterName,
					ApplicationClusterFollowerEvent.class.getName());
		}
		leaderInfo.setLeader(true);
		instanceId.setLeaderInfo(leaderInfo);
		log.info("Leader's info: " + leaderInfo);

		final String key = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName;
		redisTemplate.opsForList().leftPush(key, instanceId.getApplicationInfo());
		if (instanceId.isLeader()) {
			ttlKeeper.keepAlive(key, leaderLease, 1, TimeUnit.SECONDS);
		}

		applicationContext.publishEvent(new ApplicationClusterRefreshedEvent(applicationContext, leaderInfo));
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return eventType == ConsistencyRequestConfirmationEvent.class;
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return sourceType == ApplicationInfo.class;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		final ConsistencyRequestConfirmationEvent event = (ConsistencyRequestConfirmationEvent) applicationEvent;
		final String leaderIdentify = Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":leader";
		if (leaderIdentify.equals(event.getRequest().getName())) {
			if (event.isOk()) {
				onTriggered(event);
			} else {
				throw new LeaderNotFoundException();
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
