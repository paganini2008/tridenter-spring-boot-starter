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
package io.atlantisframework.tridenter.multiprocess;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.multicast.ApplicationMessageListener;

/**
 * 
 * MultiProcessingCompletionListener
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class MultiProcessingCompletionListener implements ApplicationMessageListener {

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Override
	public void onMessage(ApplicationInfo applicationInfo, String id, Object message) {
		redisMessageSender.sendMessage(((Return) message).getInvocation().getId(), message);
	}

	@Override
	public String getTopic() {
		return MultiProcessingCompletionListener.class.getName();
	}

}
