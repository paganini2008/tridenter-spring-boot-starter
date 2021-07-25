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
package indi.atlantis.framework.tridenter.http;

import java.lang.reflect.Type;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import indi.atlantis.framework.tridenter.http.Statistic.Permit;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RequestTemplate
 *
 * @author Fred Feng
 * @version 1.0
 */
@SuppressWarnings("unchecked")
@Slf4j
public final class RequestTemplate {

	private final RequestProcessor requestProcessor;
	private final StatisticIndicator statisticIndicator;
	private final RequestInterceptorContainer requestInterceptorContainer;

	public RequestTemplate(RoutingAllocator routingAllocator, RestClientPerformer restClientPerformer,
			RetryTemplateFactory retryTemplateFactory, AsyncTaskExecutor taskExecutor,
			RequestInterceptorContainer requestInterceptorContainer, StatisticIndicator statisticIndicator) {
		this(new DefaultRequestProcessor(routingAllocator, restClientPerformer, retryTemplateFactory, taskExecutor),
				requestInterceptorContainer, statisticIndicator);
	}

	public RequestTemplate(RequestProcessor requestProcessor, RequestInterceptorContainer requestInterceptorContainer,
			StatisticIndicator statisticIndicator) {
		this.requestInterceptorContainer = requestInterceptorContainer;
		this.requestProcessor = requestProcessor;
		this.statisticIndicator = statisticIndicator;
	}

	public <T> ResponseEntity<T> sendRequest(String provider, Request req, Type responseType) {
		ResponseEntity<T> responseEntity = null;
		RestClientException reason = null;
		final ForwardedRequest request = (ForwardedRequest) req;
		int retries = request.getRetries();
		int timeout = request.getTimeout();
		FallbackProvider fallbackProvider = request.getFallback();

		Statistic statistic = statisticIndicator.compute(provider, request);
		Permit permit = statistic.getPermit();
		try {
			if (permit.getAvailablePermits() < 1) {
				throw new RestfulException(request, InterruptedType.TOO_MANY_REQUESTS);
			}
			permit.accquire();
			if (requestInterceptorContainer.beforeSubmit(provider, request)) {
				if (retries > 0 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithRetryAndTimeout(provider, request, responseType, retries, timeout);
				} else if (retries < 1 && timeout > 0) {
					responseEntity = requestProcessor.sendRequestWithTimeout(provider, request, responseType, timeout);
				} else if (retries > 0 && timeout < 1) {
					responseEntity = requestProcessor.sendRequestWithRetry(provider, request, responseType, retries);
				} else {
					responseEntity = requestProcessor.sendRequest(provider, request, responseType);
				}
			}
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
			responseEntity = executeFallback(provider, request, responseType, e, fallbackProvider);
			reason = e;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			permit.release();
			requestInterceptorContainer.afterSubmit(provider, request, responseEntity, reason);
		}
		if (responseEntity == null) {
			responseEntity = executeFallback(provider, request, responseType, reason, fallbackProvider);
		}
		return responseEntity;
	}

	public <T> ResponseEntity<T> sendRequest(String provider, Request req, RestTemplateCallback<T> callback) {
		ResponseEntity<T> responseEntity = null;
		RestClientException reason = null;
		final ForwardedRequest request = (ForwardedRequest) req;
		int retries = request.getRetries();
		int timeout = request.getTimeout();
		FallbackProvider fallback = request.getFallback();

		Statistic statistic = statisticIndicator.compute(provider, request);
		Permit permit = statistic.getPermit();
		try {
			if (permit.getAvailablePermits() < 1) {
				throw new RestfulException(request, InterruptedType.TOO_MANY_REQUESTS);
			}
			permit.accquire();
			T body = null;
			if (requestInterceptorContainer.beforeSubmit(provider, request)) {
				if (retries > 0 && timeout > 0) {
					body = requestProcessor.sendRequestWithRetryAndTimeout(provider, request, callback, retries, timeout);
				} else if (retries < 1 && timeout > 0) {
					body = requestProcessor.sendRequestWithTimeout(provider, request, callback, timeout);
				} else if (retries > 0 && timeout < 1) {
					body = requestProcessor.sendRequestWithRetry(provider, request, callback, retries);
				} else {
					body = requestProcessor.sendRequest(provider, request, callback);
				}
				responseEntity = new ResponseEntity<T>(body, request.getHeaders(), HttpStatus.OK);
			}
		} catch (RestClientException e) {
			log.error(e.getMessage(), e);
			responseEntity = executeFallback(provider, request, null, e, fallback);
			reason = e;
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			permit.release();
			requestInterceptorContainer.afterSubmit(provider, request, responseEntity, reason);
		}
		if (responseEntity == null) {
			responseEntity = executeFallback(provider, request, null, reason, fallback);
		}
		return responseEntity;
	}

	protected <T> ResponseEntity<T> executeFallback(String provider, Request request, Type responseType, RestClientException e,
			FallbackProvider fallback) {
		if (fallback == null) {
			throw e;
		}
		try {
			if (fallback.hasFallback(provider, request, responseType, e)) {
				T body = (T) fallback.getBody(provider, request, responseType, e);
				return new ResponseEntity<T>(body, fallback.getHeaders(), fallback.getHttpStatus());
			}
		} catch (Exception fallbackError) {
			throw RestClientUtils.wrapException("Failed to execute fallback", fallbackError, request);
		}
		throw e;
	}

}
