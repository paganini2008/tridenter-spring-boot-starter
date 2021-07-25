/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package indi.atlantis.framework.tridenter.multiprocess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;
import com.github.paganini2008.springdessert.reditools.common.SharedLatch;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;
import indi.atlantis.framework.tridenter.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ProcessPoolTaskListener
 *
 * @author Fred Feng
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
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		if (log.isTraceEnabled()) {
			log.trace("Invocation: {}, from: {}, to: {}", message, applicationInfo.getId(), instanceId.get());
		}
		final Invocation invocation = (Invocation) message;
		final Signature signature = invocation.getSignature();

		final Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean != null) {
			Object result = null;
			try {
				invocationBarrier.complete();
				result = MethodUtils.invokeMethod(bean, signature.getMethodName(), invocation.getArguments());
				if (StringUtils.isNotBlank(signature.getSuccessMethodName())) {
					applicationMulticastGroup.unicast(applicationName, MultiProcessingCallbackListener.class.getName(),
							new SuccessCallback(invocation, result));
				}
			} finally {
				applicationMulticastGroup.unicast(applicationName, MultiProcessingCompletionListener.class.getName(),
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
