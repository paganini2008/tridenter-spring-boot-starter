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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMessageListener;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * CcrRequestPreparationListener
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class CcrRequestPreparationListener implements ApplicationMessageListener {

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

		final String anotherInstanceId = applicationInfo.getId();
		if (log.isTraceEnabled()) {
			log.trace(getTopic() + " " + anotherInstanceId + ", " + message);
		}
		long batchNo = request.getBatchNo();
		long serialNo = request.getSerialNo();
		long maxSerialNo = ccrRequestLocal.getSerialNo(name, batchNo);
		if (serialNo > maxSerialNo) {
			Object value = ccrRequestLocal.setValue(name, batchNo, serialNo, request.getValue());
			request.setValue(value);
			applicationMulticastGroup.send(anotherInstanceId, CcrRequest.PREPARATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), true));
		} else {
			applicationMulticastGroup.send(anotherInstanceId, CcrRequest.PREPARATION_RESPONSE,
					request.ack(instanceId.getApplicationInfo(), false));
		}
	}

	@Override
	public String getTopic() {
		return CcrRequest.PREPARATION_REQUEST;
	}

}
