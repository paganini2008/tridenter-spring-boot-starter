package org.springtribe.framework.cluster.http;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springtribe.framework.cluster.ApplicationClusterContext;

import com.github.paganini2008.devtools.proxy.ProxyFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientProxyFactoryBean
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class RestClientProxyFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

	private final Class<T> interfaceClass;

	public RestClientProxyFactoryBean(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Autowired
	private ApplicationClusterContext applicationClusterContext;

	@Autowired
	private RequestTemplate requestTemplate;

	private ConfigurableBeanFactory beanFactory;

	@SuppressWarnings("unchecked")
	@Override
	public T getObject() throws Exception {
		final RestClient restClient = interfaceClass.getAnnotation(RestClient.class);
		final String provider = beanFactory.resolveEmbeddedValue(restClient.provider());
		log.info("Create RestClient for provider: {}", provider);
		return (T) ProxyFactory.getDefault().getProxy(null,
				new RestClientBeanAspect(provider, restClient, interfaceClass, applicationClusterContext, requestTemplate),
				new Class<?>[] { interfaceClass });
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

}
