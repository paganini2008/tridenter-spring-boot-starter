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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.Constants;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;

/**
 * 
 * ApplicationMulticastStarterListener
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ApplicationMulticastStarterListener implements RedisMessageHandler, ApplicationContextAware {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Autowired
	private InstanceId instanceId;

	private ApplicationContext applicationContext;

	@Override
	public void onMessage(String channel, Object message) {
		final ApplicationInfo applicationInfo = (ApplicationInfo) message;
		if (!applicationMulticastGroup.hasRegistered(applicationInfo)) {
			applicationMulticastGroup.registerCandidate(applicationInfo);
			redisMessageSender.sendMessage(getChannel(), instanceId.getApplicationInfo());
			applicationContext
					.publishEvent(new ApplicationMulticastEvent(applicationContext, applicationInfo, MulticastEventType.ON_ACTIVE));
		}
	}

	@Override
	public String getChannel() {
		return Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":active";
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
