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
package io.atlantisframework.tridenter.multicast;

import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.time.DateUtils;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.http.DirectRoutingAllocator;
import io.atlantisframework.tridenter.http.ForwardedRequest;
import io.atlantisframework.tridenter.http.RequestInterceptorContainer;
import io.atlantisframework.tridenter.http.RequestTemplate;
import io.atlantisframework.tridenter.http.RestClientPerformer;
import io.atlantisframework.tridenter.http.RetryTemplateFactory;
import io.atlantisframework.tridenter.http.StatisticIndicator;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastEvent.MulticastEventType;
import io.atlantisframework.tridenter.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationHeartbeatTask
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Slf4j
public class ApplicationHeartbeatTask implements Runnable {

	private static final int DEFAULT_MINUMUN_TIMEOUT = 5;
	public static final String APPLICATION_PING_PATH = "/application/cluster/ping";

	private final int timeout;
	private final ApplicationInfo applicationInfo;

	ApplicationHeartbeatTask(ApplicationInfo applicationInfo, RestClientPerformer restClientPerformer, RetryTemplateFactory retryTemplateFactory,
			ThreadPoolTaskExecutor taskExecutor, RequestInterceptorContainer requestInterceptorContainer,
			StatisticIndicator statisticIndicator, ApplicationMulticastGroup applicationMulticastGroup, int timeout) {
		this.applicationInfo = applicationInfo;
		this.requestTemplate = new RequestTemplate(new DirectRoutingAllocator(), restClientPerformer, retryTemplateFactory, taskExecutor,
				requestInterceptorContainer, statisticIndicator);
		this.applicationMulticastGroup = applicationMulticastGroup;
		this.timeout = Integer.max(DEFAULT_MINUMUN_TIMEOUT, timeout);
	}

	private final RequestTemplate requestTemplate;
	private final ApplicationMulticastGroup applicationMulticastGroup;
	private volatile long timestamp;

	@Override
	public void run() {
		try {
			ForwardedRequest request = ForwardedRequest.getRequest(APPLICATION_PING_PATH);
			request.setTimeout(60);
			ResponseEntity<ApplicationInfo> responseEntity = requestTemplate.sendRequest(applicationInfo.getApplicationContextPath(),
					request, ApplicationInfo.class);
			if (log.isTraceEnabled()) {
				log.trace("Heartbeat info: " + responseEntity.getBody().toString());
			}
			timestamp = System.currentTimeMillis();
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
			if (System.currentTimeMillis() - timestamp > DateUtils.convertToMillis(timeout, TimeUnit.SECONDS)) {
				applicationMulticastGroup.removeCandidate(applicationInfo);
				log.warn(
						"Failed to keep heartbeating from inactive application '{}' because of timeout settings: {}. ApplicationMulticastEvent will be sent.",
						applicationInfo, timeout);
				ApplicationContextUtils.publishEvent(new ApplicationMulticastEvent(ApplicationContextUtils.getApplicationContext(),
						applicationInfo, MulticastEventType.ON_INACTIVE));
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}
	}

}
