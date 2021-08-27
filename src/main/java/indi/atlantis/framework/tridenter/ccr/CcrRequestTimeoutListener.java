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
package indi.atlantis.framework.tridenter.ccr;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CcrRequestTimeoutListener
 *
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@Slf4j
public class CcrRequestTimeoutListener implements ApplicationMessageListener, ApplicationContextAware {

	@Qualifier("batchNoGenerator")
	@Autowired
	private CcrSerialNoGenerator batchNoGenerator;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

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
		applicationContext.publishEvent(new CcrRequestConfirmationEvent(request, applicationInfo, false));
		applicationMulticastGroup.send(anotherInstanceId, CcrRequest.TIMEOUT_RESPONSE, request);
	}

	@Override
	public String getTopic() {
		return CcrRequest.TIMEOUT_REQUEST;
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}