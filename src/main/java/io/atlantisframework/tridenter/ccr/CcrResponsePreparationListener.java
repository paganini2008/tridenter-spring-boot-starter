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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.multicast.ApplicationMessageListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CcrResponsePreparationListener
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class CcrResponsePreparationListener implements ApplicationMessageListener {

	@Autowired
	private CcrPlatform ccrPlatform;

	@Qualifier("batchNoGenerator")
	@Autowired
	private CcrSerialNoGenerator batchNoGenerator;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		final CcrResponse response = (CcrResponse) message;
		final CcrRequest request = response.getRequest();
		final String name = request.getName();
		if (request.getBatchNo() != batchNoGenerator.currentSerialNo(name)) {
			if (log.isTraceEnabled()) {
				log.trace("This round of proposal '{}' has been finished.", name);
			}
			return;
		}
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + applicationInfo.getId() + ", " + response);
		}
		if (response.isAcceptable()) {
			ccrPlatform.canCommit(response);
		}
	}

	@Override
	public String getTopic() {
		return CcrRequest.PREPARATION_RESPONSE;
	}

}
