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
package io.atlantisframework.tridenter.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.converter.ConvertUtils;

/**
 * 
 * BeanExpressionUtils
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@Component
public class BeanExpressionUtils implements BeanFactoryAware {

	private static final SpringContextHolder contextHolder = new SpringContextHolder();
	private static final BeanExpressionResolver EXPRESSION_RESOLVER = new StandardBeanExpressionResolver();

	static class SpringContextHolder {

		ConfigurableBeanFactory beanFactory;

		public ConfigurableBeanFactory getConfigurableBeanFactory() {
			Assert.isNull(beanFactory, new IllegalStateException("Nullable ConfigurableBeanFactory."));
			return beanFactory;
		}
	}

	public static <T> T resolveExpression(String expression, Class<T> requiredType) {
		ConfigurableBeanFactory beanFactory = contextHolder.getConfigurableBeanFactory();
		String stringValue = beanFactory.resolveEmbeddedValue(expression);
		Object result = EXPRESSION_RESOLVER.evaluate(stringValue, new BeanExpressionContext(beanFactory, null));
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		try {
			return typeConverter.convertIfNecessary(result, requiredType);
		} catch (RuntimeException e) {
			return ConvertUtils.convertValue(result, requiredType);
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		contextHolder.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

}
