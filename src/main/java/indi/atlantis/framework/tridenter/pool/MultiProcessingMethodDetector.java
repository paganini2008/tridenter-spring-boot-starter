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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.reflection.MethodUtils;

/**
 * 
 * MultiProcessingMethodDetector
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class MultiProcessingMethodDetector implements BeanPostProcessor {

	private final Map<String, Signature> metadata = new ConcurrentHashMap<String, Signature>();

	public Signature getSignature(String serviceName) {
		return metadata.get(serviceName);
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		List<Method> methodList = MethodUtils.getMethodsWithAnnotation(bean.getClass(), MultiProcessing.class);
		if (CollectionUtils.isNotEmpty(methodList)) {
			for (Method method : methodList) {
				MultiProcessing anno = method.getAnnotation(MultiProcessing.class);
				metadata.putIfAbsent(anno.value(),
						new MethodSignature(beanName, ClassUtils.getUserClass(bean.getClass()).getName(), method.getName()));
			}
			List<Method> callbackMethodList = MethodUtils.getMethodsWithAnnotation(bean.getClass(), OnSuccess.class);
			for (Method method : callbackMethodList) {
				OnSuccess anno = method.getAnnotation(OnSuccess.class);
				String ref = anno.value();
				MethodSignature signature = (MethodSignature) metadata.get(ref);
				signature.setSuccessMethodName(method.getName());
			}
			callbackMethodList = MethodUtils.getMethodsWithAnnotation(bean.getClass(), OnFailure.class);
			for (Method method : callbackMethodList) {
				OnFailure anno = method.getAnnotation(OnFailure.class);
				String ref = anno.value();
				MethodSignature signature = (MethodSignature) metadata.get(ref);
				signature.setFailureMethodName(method.getName());
			}
		}
		return bean;
	}

}
