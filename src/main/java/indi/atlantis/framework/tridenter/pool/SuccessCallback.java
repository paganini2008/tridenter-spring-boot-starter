/**
* Copyright 2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter.pool;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * SuccessCallback
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class SuccessCallback extends Return implements Callback {

	private static final long serialVersionUID = 267993165180485661L;

	public SuccessCallback() {
	}

	SuccessCallback(Invocation invocation, Object returnValue) {
		super(invocation, returnValue);
	}

	@JsonIgnore
	public String getMethodName() {
		return getInvocation().getSignature().getSuccessMethodName();
	}

	@JsonIgnore
	public Object[] getArguments() {
		return new Object[] { getReturnValue(), getInvocation() };
	}
}
