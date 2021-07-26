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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.StringUtils;

/**
 * 
 * RestClientUtils
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public abstract class RestClientUtils {

	public static HttpStatus getHttpStatus(Throwable e) {
		return getHttpStatus(e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public static HttpStatus getHttpStatus(Throwable e, HttpStatus defaultHttpStatus) {
		if (e instanceof RestClientException) {
			if (e instanceof RestfulException) {
				return ((RestfulException) e).getInterruptedType().getHttpStatus();
			} else if (e instanceof HttpStatusCodeException) {
				return ((HttpStatusCodeException) e).getStatusCode();
			}
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
		return defaultHttpStatus;
	}

	public static ResponseEntity<String> getErrorResponse(Throwable e) {
		return getErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public static ResponseEntity<String> getErrorResponse(Throwable e, HttpStatus defaultHttpStatus) {
		HttpStatus httpStatus = defaultHttpStatus;
		if (e instanceof RestClientException) {
			if (e instanceof RestfulException) {
				httpStatus = ((RestfulException) e).getInterruptedType().getHttpStatus();
			} else if (e instanceof HttpStatusCodeException) {
				httpStatus = ((HttpStatusCodeException) e).getStatusCode();
			}
		}
		String msg = e.getMessage();
		if (StringUtils.isBlank(msg)) {
			msg = httpStatus.getReasonPhrase();
		}
		return new ResponseEntity<String>(msg, httpStatus);
	}

	public static RestfulException wrapException(String msg, Throwable e, Request request) {
		return wrapException(msg, e, request, InterruptedType.INTERNAL_SERVER_ERROR);
	}

	public static RestfulException wrapException(String msg, Throwable e, Request request, InterruptedType interruptedType) {
		if (e instanceof RestClientException) {
			throw (RestClientException) e;
		}
		throw new RestfulException(msg, e, request, interruptedType);
	}

}
