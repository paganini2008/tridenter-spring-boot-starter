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
package io.atlantisframework.tridenter.ccr;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.multicast.ApplicationMessageListener;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CcrRequestLearningListener
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class CcrRequestLearningListener implements ApplicationMessageListener, ApplicationContextAware {

	@Qualifier("batchNoGenerator")
	@Autowired
	private CcrSerialNoGenerator batchNoGenerator;

	@Qualifier("serialNoGenerator")
	@Autowired
	private CcrSerialNoGenerator serialNoGenerator;

	@Autowired
	private CcrRequestLocal ccrRequestLocal;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private CcrPlatform ccrPlatform;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final CcrRequest request = (CcrRequest) message;
		final String name = request.getName();
		if (request.getBatchNo() != batchNoGenerator.currentSerialNo(name)) {
			if (log.isTraceEnabled()) {
				log.trace("This round of proposal '{}' has been finished.", name);
			}
			return;
		}
		final String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + request);
		}
		if (log.isDebugEnabled()) {
			log.debug("Selected CcrRequest: " + request);
		}
		clean(name);
		ccrPlatform.completeProposal(name);

		applicationContext.publishEvent(new CcrRequestConfirmationEvent(request, applicationInfo, true));
		applicationMulticastGroup.send(anotherInstanceId, CcrRequest.LEARNING_RESPONSE, request);

	}

	@Override
	public String getTopic() {
		return CcrRequest.LEARNING_REQUEST;
	}

	private void clean(String name) {
		batchNoGenerator.clean(name);
		serialNoGenerator.clean(name);
		ccrRequestLocal.clean(name);
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
