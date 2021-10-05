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
import io.atlantisframework.tridenter.InstanceId;
import io.atlantisframework.tridenter.multicast.ApplicationMessageListener;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CcrRequestCommitmentListener
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class CcrRequestCommitmentListener implements ApplicationMessageListener {

	@Qualifier("batchNoGenerator")
	@Autowired
	private CcrSerialNoGenerator batchNoGenerator;

	@Autowired
	private CcrRequestLocal ccrRequestLocal;

	@Autowired
	private InstanceId instanceId;

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

		String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + request);
		}
		long batchNo = request.getBatchNo();
		long serialNo = request.getSerialNo();
		long maxSerialNo = ccrRequestLocal.getSerialNo(name, batchNo);
		if (serialNo >= maxSerialNo) {
			ccrRequestLocal.setValue(name, batchNo, serialNo, request.getValue());
			applicationMulticastGroup.send(anotherInstanceId, CcrRequest.COMMITMENT_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), true));
		} else {
			applicationMulticastGroup.send(anotherInstanceId, CcrRequest.COMMITMENT_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), false));
		}
	}

	@Override
	public String getTopic() {
		return CcrRequest.COMMITMENT_REQUEST;
	}

}
