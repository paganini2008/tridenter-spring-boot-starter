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

import org.springframework.web.client.RestClientException;

/**
 * 
 * RestfulException
 *
 * @author Fred Feng
 * 
 * @since 2.0.1
 */
public class RestfulException extends RestClientException {

	private static final long serialVersionUID = -8762523199569525919L;

	public RestfulException(Request request, InterruptedType interruptedType) {
		this(request.toString(), request, interruptedType);
	}

	public RestfulException(String msg, Request request, InterruptedType interruptedType) {
		super(msg);
		this.request = request;
		this.interruptedType = interruptedType;
	}

	public RestfulException(String msg, Throwable e, Request request, InterruptedType interruptedType) {
		super(msg, e);
		this.request = request;
		this.interruptedType = interruptedType;
	}

	private final Request request;
	private final InterruptedType interruptedType;

	public Request getRequest() {
		return request;
	}

	public InterruptedType getInterruptedType() {
		return interruptedType;
	}

}