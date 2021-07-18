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

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.StringUtils;

import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;
import indi.atlantis.framework.tridenter.utils.BeanExpressionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MultiProcessingInterpreter
 *
 * @author Fred Feng
 * @version 1.0
 */
@Slf4j
@Aspect
public class MultiProcessingInterpreter {

	@Value("${spring.application.name}")
	private String applicationName;

	@Autowired
	private ProcessPool processPool;

	@Autowired
	private MultiProcessingMethodInspector methodInspector;

	@Autowired
	private InvocationBarrier invocationBarrier;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(multiProcessing)")
	public Object arround(ProceedingJoinPoint pjp, MultiProcessing multiProcessing) throws Throwable {
		final Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		if (invocationBarrier.isCompleted()) {
			try {
				return pjp.proceed();
			} catch (Throwable e) {
				if (ExceptionUtils.ignoreException(e, multiProcessing.ignoredFor())) {
					Signature signature = methodInspector
							.getSignature(org.springframework.util.ClassUtils.getUserClass(method.getDeclaringClass()), method.getName());
					if (StringUtils.isNotBlank(signature.getFailureMethodName())) {
						applicationMulticastGroup.unicast(applicationName, MultiProcessingCallbackListener.class.getName(),
								new FailureCallback(new MethodInvocation(signature, pjp.getArgs()), e));
					}
					if (StringUtils.isNotBlank(multiProcessing.defaultValue())) {
						return BeanExpressionUtils.resolveExpression(multiProcessing.defaultValue(), method.getReturnType());
					} else {
						return ClassUtils.getNullableValue(method.getReturnType());
					}
				}
				throw e;
			}
		} else {
			Signature signature = methodInspector.getSignature(org.springframework.util.ClassUtils.getUserClass(method.getDeclaringClass()),
					method.getName());
			MethodInvocation invocation = new MethodInvocation(signature, pjp.getArgs());
			try {
				if (multiProcessing.async()) {
					processPool.execute(invocation);
					return ClassUtils.getNullableValue(method.getReturnType());
				} else {
					TaskPromise promise = processPool.submit(invocation);
					Supplier<Object> defaultValue = StringUtils.isNotBlank(multiProcessing.defaultValue()) ? () -> {
						return BeanExpressionUtils.resolveExpression(multiProcessing.defaultValue(), method.getReturnType());
					} : null;
					return multiProcessing.timeout() > 0 ? promise.get(multiProcessing.timeout(), TimeUnit.MILLISECONDS, defaultValue)
							: promise.get(defaultValue);
				}
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				throw e;
			}
		}
	}

}
