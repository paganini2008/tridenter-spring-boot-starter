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
package indi.atlantis.framework.tridenter.xa;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * MethodInfo
 *
 * @author Fred Feng
 *
 * @since 2.0.4
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MethodInfo {

	private int modifiers;
	private String declaringTypeName;
	private String name;
	private String[] parameterNames;
	private String[] parameterTypeNames;
	private String[] exceptionTypeNames;
	private String returnTypeName;
	private Object[] args;

	public static MethodInfo extractFrom(ProceedingJoinPoint pjp) {
		MethodInfo methodInfo = new MethodInfo();
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		methodInfo.setDeclaringTypeName(signature.getDeclaringTypeName());
		methodInfo.setModifiers(signature.getModifiers());
		methodInfo.setName(signature.getName());
		methodInfo.setParameterNames(signature.getParameterNames());
		methodInfo.setParameterTypeNames(Arrays.stream(signature.getParameterTypes()).map(c -> c.getName()).toArray(String[]::new));
		methodInfo.setExceptionTypeNames(Arrays.stream(signature.getExceptionTypes()).map(c -> c.getName()).toArray(String[]::new));
		methodInfo.setReturnTypeName(signature.getReturnType().getName());
		methodInfo.setArgs(pjp.getArgs());
		return methodInfo;
	}

}
