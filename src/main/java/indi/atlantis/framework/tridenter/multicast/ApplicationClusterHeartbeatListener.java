package indi.atlantis.framework.tridenter.multicast;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.http.RequestInterceptorContainer;
import indi.atlantis.framework.tridenter.http.RestClientPerformer;
import indi.atlantis.framework.tridenter.http.RetryTemplateFactory;
import indi.atlantis.framework.tridenter.http.StatisticIndicator;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationClusterHeartbeatListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ApplicationClusterHeartbeatListener implements ApplicationListener<ApplicationMulticastEvent> {

	private static final int DEFAULT_HEARTBEAT_INTERVAL = 1;

	@Autowired
	private InstanceId instanceId;

	@Qualifier("applicationClusterTaskScheduler")
	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private RestClientPerformer restClientPerformer;

	@Autowired
	private RetryTemplateFactory retryTemplateFactory;

	@Qualifier("applicationClusterTaskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private RequestInterceptorContainer requestInterceptorContainer;

	@Qualifier("requestStatistic")
	@Autowired
	private StatisticIndicator statisticIndicator;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Value("${spring.application.cluster.multicast.heartbeat.timeout:5}")
	private int timeout;

	private final Map<ApplicationInfo, ScheduledFuture<?>> applicationFutures = new ConcurrentHashMap<ApplicationInfo, ScheduledFuture<?>>();

	@Override
	public void onApplicationEvent(ApplicationMulticastEvent event) {
		final ApplicationInfo applicationInfo = event.getApplicationInfo();
		MulticastEventType eventType = event.getMulticastEventType();
		if (eventType == MulticastEventType.ON_ACTIVE) {
			if (!instanceId.getApplicationInfo().equals(event.getApplicationInfo())) {
				ApplicationHeartbeatTask heartbeatTask = new ApplicationHeartbeatTask(applicationInfo, restClientPerformer,
						retryTemplateFactory, taskExecutor, requestInterceptorContainer, statisticIndicator, applicationMulticastGroup,
						timeout);
				applicationFutures.put(applicationInfo,
						taskScheduler.scheduleWithFixedDelay(heartbeatTask, Duration.ofSeconds(DEFAULT_HEARTBEAT_INTERVAL)));
				log.info("Keep heartbeating from application '{}' with fixed delay {} second.", applicationInfo.getId(),
						DEFAULT_HEARTBEAT_INTERVAL);
			}
		} else if (eventType == MulticastEventType.ON_INACTIVE) {
			ScheduledFuture<?> applicationFuture = applicationFutures.remove(event.getApplicationInfo());
			if (applicationFuture != null) {
				try {
					applicationFuture.cancel(true);
				} catch (Exception ignored) {
				}
				log.info("Cancel heartbeating from application '{}' with fixed delay.", applicationInfo.getId());
			}
		}

	}

}
