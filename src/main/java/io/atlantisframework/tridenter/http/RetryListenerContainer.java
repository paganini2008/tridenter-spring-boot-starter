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

import static io.atlantisframework.tridenter.http.RequestProcessor.CURRENT_RETRY_IDENTIFIER;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import io.atlantisframework.tridenter.http.DefaultRequestProcessor.RetryEntry;

/**
 * 
 * RetryListenerContainer
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class RetryListenerContainer implements RetryListener {

	private final List<RetryListener> retryListeners = new CopyOnWriteArrayList<RetryListener>();
	private final List<ApiRetryListener> apiRetryListeners = new CopyOnWriteArrayList<ApiRetryListener>();

	public RetryListenerContainer() {
		retryListeners.add(this);
	}

	public void addListener(ApiRetryListener listener) {
		if (listener != null) {
			apiRetryListeners.add(listener);
		}
	}

	public void removeListener(ApiRetryListener listener) {
		if (listener != null) {
			apiRetryListeners.remove(listener);
		}
	}

	public void addListener(RetryListener listener) {
		if (listener != null) {
			retryListeners.add(listener);
		}
	}

	public void removeListener(RetryListener listener) {
		if (listener != null) {
			retryListeners.remove(listener);
		}
	}

	public List<RetryListener> getRetryListeners() {
		return retryListeners;
	}

	public List<ApiRetryListener> getApiRetryListeners() {
		return apiRetryListeners;
	}

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (context.hasAttribute(CURRENT_RETRY_IDENTIFIER)) {
			RetryEntry retryEntry = (RetryEntry) context.getAttribute(CURRENT_RETRY_IDENTIFIER);
			String provider = retryEntry.getProvider();
			Request request = retryEntry.getRequest();
			int retryCount = context.getRetryCount();
			apiRetryListeners.forEach(listener -> {
				if (listener.matches(provider, request)) {
					if (retryCount == 1) {
						listener.onRetryBegin(provider, request);
					}
					listener.onEachRetry(provider, request, throwable);
					
					if (retryCount == retryEntry.getMaxAttempts()) {
						listener.onRetryEnd(provider, request, throwable);
					}
				}
			});
		}
	}

}
