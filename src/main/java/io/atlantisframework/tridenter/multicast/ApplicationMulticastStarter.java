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
package io.atlantisframework.tridenter.multicast;

import static io.atlantisframework.tridenter.ClusterConstants.APPLICATION_CLUSTER_NAMESPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.github.paganini2008.springdessert.reditools.messager.RedisMessageSender;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.InstanceId;

/**
 * 
 * ApplicationMulticastStarter
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class ApplicationMulticastStarter implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private RedisMessageSender redisMessageSender;

	@Autowired
	private InstanceId instanceId;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		final String channel = APPLICATION_CLUSTER_NAMESPACE + clusterName + ":active";
		redisMessageSender.sendMessage(channel, applicationInfo);
	}

}
