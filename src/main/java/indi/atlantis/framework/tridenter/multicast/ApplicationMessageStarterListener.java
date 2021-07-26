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
package indi.atlantis.framework.tridenter.multicast;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup.MulticastMessage;

/**
 * 
 * ApplicationMessageStarterListener
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public class ApplicationMessageStarterListener implements RedisMessageHandler, ApplicationContextAware {

	@Autowired
	private InstanceId instanceId;

	private ApplicationContext applicationContext;

	@Override
	public void onMessage(String channel, Object object) {
		MulticastMessage messageObject = (MulticastMessage) object;
		ApplicationInfo applicationInfo = messageObject.getApplicationInfo();
		ApplicationMulticastEvent multicastEvent = new ApplicationMulticastEvent(applicationContext, applicationInfo,
				MulticastEventType.ON_MESSAGE);
		multicastEvent.setMessage(messageObject);
		applicationContext.publishEvent(multicastEvent);
	}

	@Override
	public String getChannel() {
		return instanceId.get();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
