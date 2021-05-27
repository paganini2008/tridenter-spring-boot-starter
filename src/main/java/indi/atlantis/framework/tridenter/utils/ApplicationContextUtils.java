package indi.atlantis.framework.tridenter.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.Assert;
import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.beans.BeanUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationContextUtils
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
@Component
@Slf4j
public class ApplicationContextUtils implements ApplicationContextAware {

	private static final SpringContextHolder contextHolder = new SpringContextHolder();

	static class SpringContextHolder {

		ApplicationContext applicationContext;
		AutowireCapableBeanFactory beanFactory;
		Environment environment;

		public ApplicationContext getApplicationContext() {
			Assert.isNull(applicationContext, new IllegalStateException("Nullable ApplicationContext."));
			return applicationContext;
		}

		public AutowireCapableBeanFactory getBeanFactory() {
			Assert.isNull(beanFactory, new IllegalStateException("Nullable beanFactory."));
			return beanFactory;
		}

		public Environment getEnvironment() {
			Assert.isNull(environment, new IllegalStateException("Nullable environment."));
			return environment;
		}

	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		contextHolder.applicationContext = applicationContext;
		contextHolder.beanFactory = applicationContext.getAutowireCapableBeanFactory();
		contextHolder.environment = applicationContext.getEnvironment();
	}

	public static ApplicationContext getApplicationContext() {
		return contextHolder.getApplicationContext();
	}

	public static AutowireCapableBeanFactory getBeanFactory() {
		return contextHolder.getBeanFactory();
	}

	public static Environment getEnvironment() {
		return contextHolder.getEnvironment();
	}

	public static void publishEvent(ApplicationEvent event) {
		getApplicationContext().publishEvent(event);
	}

	public static synchronized int countOfBeans() {
		return getApplicationContext().getBeanDefinitionCount();
	}

	public static synchronized Map<String, Object> getAllBeans() {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] beanNames = getApplicationContext().getBeanDefinitionNames();
		for (String beanName : beanNames) {
			map.put(beanName, getBean(beanName));
		}
		return map;
	}

	public static synchronized <T> List<T> getBeans(Class<T> requiredType, Function<T, Boolean> f) {
		List<T> list = new ArrayList<T>();
		for (T bean : getBeansOfType(requiredType).values()) {
			if (f.apply(bean)) {
				list.add(bean);
			}
		}
		return list;
	}

	public static synchronized <T> T getBean(Class<T> requiredType, Function<T, Boolean> f) {
		for (T bean : getBeansOfType(requiredType).values()) {
			if (f.apply(bean)) {
				return bean;
			}
		}
		return null;
	}

	public static synchronized <T> Map<String, T> getBeansOfType(Class<T> requiredType) {
		try {
			return getApplicationContext().getBeansOfType(requiredType);
		} catch (RuntimeException e) {
			log.warn("Not Found Beans of Type: {}, Reason: {}", requiredType.getName(), e.getMessage());
			return new HashMap<String, T>();
		}
	}

	public static synchronized <T> Map<String, T> getBeansOfType(Class<T> requiredType, boolean includeNonSingletons,
			boolean allowEagerInit) {
		try {
			return getApplicationContext().getBeansOfType(requiredType, includeNonSingletons, allowEagerInit);
		} catch (RuntimeException e) {
			log.warn("Not Found Beans of Type: {}, Reason: {}", requiredType.getName(), e.getMessage());
			return new HashMap<String, T>();
		}
	}

	public static synchronized Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		try {
			return getApplicationContext().getBeansWithAnnotation(annotationType);
		} catch (RuntimeException e) {
			log.warn("Not Found Beans of annotation type: {}, Reason: {}", annotationType.getName(), e.getMessage());
			return new HashMap<String, Object>();
		}
	}

	public static synchronized <T> T getBean(String name) {
		try {
			return (T) getApplicationContext().getBean(name);
		} catch (RuntimeException e) {
			log.warn("Bean '{}' have not found, Reason: {}", name, e.getMessage());
			return null;
		}
	}

	public static synchronized <T> T getBean(Class<T> requiredType) {
		try {
			return getApplicationContext().getBean(requiredType);
		} catch (RuntimeException e) {
			log.warn("Bean '{}' have not found, Reason: {}", requiredType.getName(), e.getMessage());
			return null;
		}
	}

	public static synchronized <T> T getOrCreateBean(String name, Class<T> requiredType) {
		try {
			return getApplicationContext().getBean(name, requiredType);
		} catch (RuntimeException e) {
			return instantiateClass(requiredType);
		}
	}

	public static synchronized <T> T getOrCreateBean(Class<T> requiredType) {
		try {
			return getApplicationContext().getBean(requiredType);
		} catch (RuntimeException e) {
			return instantiateClass(requiredType);
		}
	}

	public static synchronized <T> T getBean(String name, Class<T> requiredType) {
		if (StringUtils.isBlank(name)) {
			return getBean(requiredType);
		}
		try {
			return getApplicationContext().getBean(name, requiredType);
		} catch (RuntimeException e) {
			log.warn("Bean '{}' have not found, Reason: {}", requiredType.getName(), e.getMessage());
			return null;
		}
	}

	public static synchronized <T> T autowireBean(T bean) {
		getBeanFactory().autowireBean(bean);
		return bean;
	}

	public static synchronized <T> T instantiateClass(Class<T> clazz, Object... arguments) {
		T bean = BeanUtils.instantiate(clazz, arguments);
		return autowireBean(bean);
	}

	public static String getRequiredProperty(String key) {
		return getEnvironment().getRequiredProperty(key);
	}

	public static <T> T getProperty(String key, Class<T> requiredType) {
		return getEnvironment().getProperty(key, requiredType);
	}

	public static <T> T getProperty(String key, Class<T> requiredType, T defaultValue) {
		return getEnvironment().getProperty(key, requiredType, defaultValue);
	}

	public static String getProperty(String key, String defaultValue) {
		return getEnvironment().getProperty(key, defaultValue);
	}

	public static String getProperty(String key) {
		return getEnvironment().getProperty(key);
	}

}
