package indi.atlantis.framework.tridenter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.retry.support.RetryTemplate;

import com.github.paganini2008.springworld.reditools.common.ConnectionFailureHandler;
import com.github.paganini2008.springworld.reditools.common.RedisKeepAliveResolver;

import indi.atlantis.framework.tridenter.http.RetryTemplateFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RedisConnectionFailureHandler
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class RedisConnectionFailureHandler implements ConnectionFailureHandler, ApplicationContextAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisKeepAliveResolver redisKeepAliveResolver;

	@Autowired
	private RetryTemplateFactory retryTemplateFactory;

	private ApplicationContext applicationContext;

	@Override
	public void handleException(Throwable e) {
		log.warn("RedisConnection Refused");
		final RetryTemplate retryTemplate = retryTemplateFactory.setRetryPolicy(3).createObject();
		retryTemplate.execute(context -> {
			return redisKeepAliveResolver.ping();
		}, context -> {
			Throwable reason = context.getLastThrowable();
			if (reason != null) {
				log.error(reason.getMessage(), reason);
			}
			redisKeepAliveResolver.addListener(RedisConnectionFailureHandler.this);
			applicationContext.publishEvent(new ApplicationClusterFatalEvent(applicationContext, reason));
			log.warn("Application cluster '{}' will be unserviceable due to redisConnection refused.", clusterName);
			return "";
		});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
