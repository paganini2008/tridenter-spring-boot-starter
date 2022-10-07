/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter.multiprocess;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.ClassUtils;
import com.github.paganini2008.devtools.ExceptionUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;

import io.atlantisframework.tridenter.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ParallelizingCallInterpreter
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@Slf4j
@Aspect
public class ParallelizingCallInterpreter {

	@Autowired
	private MultiProcessingMethodInspector methodInspector;

	@Pointcut("execution(public * *(..))")
	public void signature() {
	}

	@Around("signature() && @annotation(parallelizing)")
	public Object arround(ProceedingJoinPoint pjp, ParallelizingCall parallelizing) throws Throwable {
		final Method method = ((org.aspectj.lang.reflect.MethodSignature) pjp.getSignature()).getMethod();
		if (method.isAnnotationPresent(MultiProcessing.class)) {
			throw new UnsupportedOperationException(
					"Either annotation 'ParallelizingCall' or 'MultiProcessing' is to decorate on target method.");
		}
		Object[] args = pjp.getArgs();
		if (ArrayUtils.isEmpty(args)) {
			throw new IllegalArgumentException("No arguments");
		}

		Signature signature = methodInspector.getSignature(org.springframework.util.ClassUtils.getUserClass(method.getDeclaringClass()),
				parallelizing.value());
		Object bean = ApplicationContextUtils.getBean(signature.getBeanName(), ClassUtils.forName(signature.getBeanClassName()));
		if (bean == null) {
			throw new NoSuchBeanDefinitionException(signature.getBeanName());
		}
		try {
			List<Object> results = new ArrayList<Object>();
			Parallelization parallelization = BeanUtils.instantiate(parallelizing.usingParallelization());
			parallelization = ApplicationContextUtils.autowireBean(parallelization);
			Object[] slices = parallelization.slice(args[0]);
			List<Object> methodArgs = new ArrayList<Object>();
			for (Object slice : slices) {
				methodArgs.add(slice);
				if (args.length > 1) {
					methodArgs.addAll(Arrays.asList(ArrayUtils.copy(args, 1)));
				}
				Object result = MethodUtils.invokeMethod(bean, signature.getMethodName(), methodArgs.toArray());
				results.add(result);
				methodArgs.clear();
			}
			return parallelization.merge(results.toArray());
		} catch (Throwable e) {
			if (ExceptionUtils.ignoreException(e, parallelizing.ignoreFor())) {
				log.error(e.getMessage(), e);
				return pjp.proceed();
			}
			throw e;
		}

	}

}
