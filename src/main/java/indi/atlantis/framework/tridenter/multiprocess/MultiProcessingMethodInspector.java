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
package indi.atlantis.framework.tridenter.multiprocess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MultiMappedMap;
import com.github.paganini2008.devtools.reflection.MethodUtils;

/**
 * 
 * MultiProcessingMethodInspector
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class MultiProcessingMethodInspector implements BeanPostProcessor {

	private final MultiMappedMap<Class<?>, String, Signature> metadata = new MultiMappedMap<Class<?>, String, Signature>();

	public Signature getSignature(Class<?> cls, String methodName) {
		return metadata.get(cls, methodName);
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> beanClass = ClassUtils.getUserClass(bean.getClass());
		boolean hasMethods = inspectMultiProcessingMethods(beanClass, beanName, MultiProcessing.class);
		hasMethods |= inspectMultiProcessingMethods(beanClass, beanName, MultiScheduling.class);
		if (hasMethods) {
			inspectMultiProcessingCallbackMethods(beanClass, beanName);
		}
		return bean;
	}

	private boolean inspectMultiProcessingMethods(Class<?> beanClass, String beanName, Class<? extends Annotation> annotationClass) {
		List<Method> methodList = MethodUtils.getMethodsWithAnnotation(beanClass, annotationClass);
		if (CollectionUtils.isNotEmpty(methodList)) {
			for (Method method : methodList) {
				metadata.put(beanClass, method.getName(), new MethodSignature(beanName, beanClass.getName(), method.getName()));
			}
		}
		return methodList.size() > 0;
	}

	private void inspectMultiProcessingCallbackMethods(Class<?> beanClass, String beanName) {
		List<Method> callbackMethodList = MethodUtils.getMethodsWithAnnotation(beanClass, OnSuccess.class);
		for (Method method : callbackMethodList) {
			OnSuccess anno = method.getAnnotation(OnSuccess.class);
			String ref = anno.value();
			MethodSignature signature = (MethodSignature) metadata.get(beanClass, ref);
			signature.setSuccessMethodName(method.getName());
		}
		callbackMethodList = MethodUtils.getMethodsWithAnnotation(beanClass, OnFailure.class);
		for (Method method : callbackMethodList) {
			OnFailure anno = method.getAnnotation(OnFailure.class);
			String ref = anno.value();
			MethodSignature signature = (MethodSignature) metadata.get(beanClass, ref);
			signature.setFailureMethodName(method.getName());
		}
	}

}
