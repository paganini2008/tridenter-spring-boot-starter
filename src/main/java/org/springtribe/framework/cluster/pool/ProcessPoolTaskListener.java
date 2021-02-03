package org.springtribe.framework.cluster.pool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springtribe.framework.cluster.ApplicationInfo;
import org.springtribe.framework.cluster.InstanceId;
import org.springtribe.framework.cluster.multicast.ApplicationMessageListener;
import org.springtribe.framework.cluster.multicast.ApplicationMulticastGroup;
import org.springtribe.framework.cluster.utils.ApplicationContextUtils;
import org.springtribe.framework.reditools.common.SharedLatch;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessPoolTaskListener
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Slf4j
public class ProcessPoolTaskListener implements ApplicationMessageListener {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private DelayQueue delayQueue;

	@Autowired
	private ProcessPool processPool;

	@Autowired
	private SharedLatch sharedLatch;

	@Autowired
	private InvocationBarrier invocationBarrier;

	@Autowired
	private ApplicationMulticastGroup multicastGroup;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		log.info("Invocation: {}, Self ID: {}, who send: {}", message, instanceId.get(), applicationInfo.getId());
		final Invocation invocation = (Invocation) message;
		final Signature signature = invocation.getSignature();

		final Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean != null) {
			Object result = null;
			try {
				invocationBarrier.setCompleted();
				result = MethodUtils.invokeMethod(bean, signature.getMethodName(), invocation.getArguments());
				if (StringUtils.isNotBlank(signature.getSuccessMethodName())) {
					multicastGroup.unicast(applicationName, MultiProcessingCallbackListener.class.getName(),
							new SuccessCallback(invocation, result));
				}
			} finally {
				multicastGroup.unicast(applicationName, MultiProcessingCompletionListener.class.getName(),
						new Return(invocation, result));

				sharedLatch.release();

				Invocation nextInvocation = (Invocation) delayQueue.pop();
				if (nextInvocation != null) {
					processPool.execute(nextInvocation);
				}
			}
		} else {
			log.warn("No bean registered in spring context to call the method of signature: " + signature);
		}
	}

	@Override
	public String getTopic() {
		return ProcessPoolTaskListener.class.getName();
	}

}
