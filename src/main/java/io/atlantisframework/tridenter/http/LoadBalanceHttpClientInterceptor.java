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
package io.atlantisframework.tridenter.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.github.paganini2008.devtools.collection.ListUtils;

import io.atlantisframework.tridenter.ApplicationInfo;
import io.atlantisframework.tridenter.LoadBalancer;
import io.atlantisframework.tridenter.multicast.ApplicationMulticastGroup;

/**
 * 
 * LoadBalanceHttpClientInterceptor
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
public class LoadBalanceHttpClientInterceptor implements ClientHttpRequestInterceptor {

	@Qualifier("applicationClusterLoadBalancer")
	@Autowired
	private LoadBalancer loadBalancer;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		final URI uri = request.getURI();
		String host = uri.getHost();
		if (applicationMulticastGroup.hasRegistered(host)) {
			List<ApplicationInfo> candidates = ListUtils.create(applicationMulticastGroup.getCandidates(host));
			ApplicationInfo selectedApplication = loadBalancer.select(host, candidates, null);
			HttpRequest newRequest = new HttpRequest() {

				@Override
				public HttpHeaders getHeaders() {
					return request.getHeaders();
				}

				@Override
				public URI getURI() {
					try {
						String location = uri.toURL().toString().replace(host, selectedApplication.getApplicationContextPath());
						return URI.create(location);
					} catch (MalformedURLException e) {
						throw new IllegalStateException(e);
					}
				}

				@Override
				public String getMethodValue() {
					return request.getMethodValue();
				}
			};
			return execution.execute(newRequest, body);
		}
		return execution.execute(request, body);
	}

}
