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

import static indi.atlantis.framework.tridenter.http.RequestProcessor.CURRENT_RETRY_IDENTIFIER;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import indi.atlantis.framework.tridenter.http.DefaultRequestProcessor.RetryEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LoggingRetryListener
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@Slf4j
public class LoggingRetryListener implements RetryListener {

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (log.isTraceEnabled()) {
			if (context.hasAttribute(CURRENT_RETRY_IDENTIFIER)) {
				RetryEntry retryEntry = (RetryEntry) context.getAttribute(CURRENT_RETRY_IDENTIFIER);
				String provider = retryEntry.getProvider();
				Request request = retryEntry.getRequest();
				log.trace("[{}] Retry: {}, Times: {}/{}", provider, request, context.getRetryCount(), retryEntry.getMaxAttempts());
			}
		}
	}

}
