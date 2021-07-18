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
package indi.atlantis.framework.tridenter.multiprocess;

import java.io.Serializable;
import java.util.UUID;

import com.github.paganini2008.devtools.Assert;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * MethodInvocation
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class MethodInvocation implements Serializable, Invocation {

	private static final long serialVersionUID = -5401293046063974728L;

	private String id;
	private Signature signature;
	private Object[] arguments;
	private long timestamp;

	public MethodInvocation(Signature signature, Object... arguments) {
		Assert.isNull(signature, "Signature not found");
		this.id = PREFIX + UUID.randomUUID().toString();
		this.signature = signature;
		this.arguments = arguments;
		this.timestamp = System.currentTimeMillis();
	}

	public MethodInvocation() {
	}

	@Override
	public int hashCode() {
		return 31 * signature.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodInvocation) {
			MethodInvocation invocation = (MethodInvocation) obj;
			return getSignature().equals(invocation.getSignature());
		}
		return false;
	}

}
