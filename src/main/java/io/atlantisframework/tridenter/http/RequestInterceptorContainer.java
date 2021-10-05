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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * RequestInterceptorContainer
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public class RequestInterceptorContainer implements BeanPostProcessor {

	private final List<RequestInterceptor> interceptors = new CopyOnWriteArrayList<RequestInterceptor>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RequestInterceptor) {
			addInterceptor((RequestInterceptor) bean);
		}
		return bean;
	}

	public void addInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.add(interceptor);
		}
	}

	public void removeInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.remove(interceptor);
		}
	}

	public boolean beforeSubmit(String provider, Request request) {
		boolean proceeded = true;
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				proceeded &= interceptor.beforeSubmit(provider, request);
			}
		}
		return proceeded;
	}

	public void afterSubmit(String provider, Request request, Object responseEntity, Throwable reason) {
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				interceptor.afterSubmit(provider, request, responseEntity, reason);
			}
		}
	}

}
