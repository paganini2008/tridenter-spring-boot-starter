/**
* Copyright 2018-2021 Fred Feng (paganini.fy@gmail.com)

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

import org.springframework.http.ResponseEntity;

/**
 * 
 * RequestProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface RequestProcessor {

	static final String CURRENT_RETRY_IDENTIFIER = "current-retry";

	<T> ResponseEntity<T> sendRequestWithRetry(String provider, Request request, Type responseType, int retries);

	<T> ResponseEntity<T> sendRequest(String provider, Request request, Type responseType);

	<T> ResponseEntity<T> sendRequestWithTimeout(String provider, Request request, Type responseType, int timeout);

	<T> ResponseEntity<T> sendRequestWithRetryAndTimeout(String provider, Request request, Type responseType, int retries, int timeout);

	<T> T sendRequestWithRetry(String provider, Request request, RestTemplateCallback<T> responseExchanger, int retries);

	<T> T sendRequest(String provider, Request request, RestTemplateCallback<T> responseExchanger);

	<T> T sendRequestWithTimeout(String provider, Request request, RestTemplateCallback<T> responseExchanger, int timeout);

	<T> T sendRequestWithRetryAndTimeout(String provider, Request request, RestTemplateCallback<T> responseExchanger, int retries,
			int timeout);
}
