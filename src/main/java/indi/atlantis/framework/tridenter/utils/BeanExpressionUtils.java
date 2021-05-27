package indi.atlantis.framework.tridenter.utils;

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
 * @since 1.0
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
