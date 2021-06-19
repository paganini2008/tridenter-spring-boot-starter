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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * MethodSignature
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@JsonInclude(value = Include.NON_NULL)
@Getter
@Setter
public class MethodSignature implements Serializable, Signature {

	private static final long serialVersionUID = 3733605394122091343L;
	private String beanName;
	private String beanClassName;
	private String methodName;
	private @Nullable String successMethodName;
	private @Nullable String failureMethodName;

	public MethodSignature() {
	}

	MethodSignature(String beanName, String beanClassName, String methodName) {
		this.beanName = beanName;
		this.beanClassName = beanClassName;
		this.methodName = methodName;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static Signature of(Method method) {
		String beanClassName = method.getDeclaringClass().getName();
		String methodName = method.getName();
		return new MethodSignature(null, beanClassName, methodName);
	}

}
