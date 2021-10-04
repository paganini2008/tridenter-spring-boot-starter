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
package indi.atlantis.framework.tridenter.xa;

import static indi.atlantis.framework.tridenter.xa.XaConstants.MESSAGE_CHANNEL_RESOURCE_MANAGER;
import static indi.atlantis.framework.tridenter.xa.XaConstants.MESSAGE_CHANNEL_TRANSACTION_MANAGER;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import indi.atlantis.framework.tridenter.InstanceId;

/**
 * 
 * RedisXaMessageSender
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
public class RedisXaMessageSender implements XaMessageSender {

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void subscribeXaMessages(final XaMessageListener messageListener) {
		redisMessageSender.subscribeChannel(messageListener.getClass().getName(), new RedisMessageHandler() {

			@Override
			public void onMessage(String channel, Object message) throws Exception {
				messageListener.onMessage(message);
			}

			@Override
			public String getChannel() {
				return messageListener.getChannel();
			}
		});
	}

	@Override
	public void ready(String xaId, long serial, MethodInfo methodInfo, XaState state, long timeout, boolean completed) {
		XaMessage event = new XaMessage(xaId, serial, methodInfo, state, timeout,completed, instanceId.getApplicationInfo());
		redisMessageSender.sendMessage(MESSAGE_CHANNEL_TRANSACTION_MANAGER, event);
	}

	@Override
	public void ack(XaMessage event, XaState state) {
		XaMessageAck ack = new XaMessageAck(event, state);
		redisMessageSender.sendMessage(MESSAGE_CHANNEL_RESOURCE_MANAGER + ":" + event.getApplicationInfo().getId(), ack);
	}

}
