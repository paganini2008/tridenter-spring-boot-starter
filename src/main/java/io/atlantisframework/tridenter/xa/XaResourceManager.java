/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter.xa;

import static io.atlantisframework.tridenter.xa.XaConstants.MESSAGE_CHANNEL_RESOURCE_MANAGER;

import org.springframework.beans.factory.annotation.Autowired;

import io.atlantisframework.tridenter.InstanceId;
import io.atlantisframework.tridenter.utils.BeanLifeCycle;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * XaResourceManager
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@Slf4j
public class XaResourceManager implements XaMessageListener, XaStateMachine, BeanLifeCycle {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private XaMessageAckPredicate messagePredicate;

	@Autowired
	private XaMessageSender messageSender;

	@Override
	public void configure() throws Exception {
		messageSender.subscribeXaMessages(this);
		log.info("XaResourceManager is ready.");
	}

	@Override
	public void beforeInvoking(String xaId, long serial, MethodInfo methodInfo, long timeout) {
		messageSender.ready(xaId, serial, methodInfo, XaState.CHECK_IN, timeout, true);
	}

	@Override
	public void afterInvoking(String xaId, long serial, MethodInfo methodInfo, long timeout, boolean completed) {
		messageSender.ready(xaId, serial, methodInfo, XaState.CHECK_OUT, timeout, completed);
	}

	@Override
	public void prepare(XaMessage message, boolean completed) {
		messageSender.ready(message.getXaId(), message.getSerial(), message.getMethodInfo(), XaState.PREPARED, message.getTimeout(),
				completed);
	}

	@Override
	public void commit(XaMessage message, boolean completed) {
		messageSender.ready(message.getXaId(), message.getSerial(), message.getMethodInfo(), XaState.COMMIT, message.getTimeout(),
				completed);
	}

	@Override
	public void rollback(XaMessage message, boolean completed) {
		messageSender.ready(message.getXaId(), message.getSerial(), message.getMethodInfo(), XaState.ROLLBACK, message.getTimeout(),
				completed);
	}

	@Override
	public String getChannel() {
		return MESSAGE_CHANNEL_RESOURCE_MANAGER + ":" + instanceId.get();
	}

	@Override
	public void onMessage(Object data) throws Exception {
		final XaMessageAck ack = (XaMessageAck) data;
		final XaMessage message = ack.getMessage();
		boolean matched = messagePredicate.test(ack);
		if (ack.getNextState() != null) {
			switch (ack.getNextState()) {
			case PREPARED:
				prepare(message, matched);
				break;
			case COMMIT:
				commit(message, matched);
				break;
			case ROLLBACK:
				rollback(message, matched);
				break;
			default:
				break;
			}
		}
	}

}
