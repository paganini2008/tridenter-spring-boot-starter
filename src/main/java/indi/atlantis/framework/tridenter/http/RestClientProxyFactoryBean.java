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
package indi.atlantis.framework.tridenter.http;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import com.github.paganini2008.devtools.proxy.ProxyFactory;

import indi.atlantis.framework.tridenter.ApplicationClusterContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientProxyFactoryBean
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
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
