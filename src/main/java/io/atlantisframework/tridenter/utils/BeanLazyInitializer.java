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
package io.atlantisframework.tridenter.utils;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.github.paganini2008.devtools.Observable;

/**
 * 
 * BeanLazyInitializer
 *
 * @author Fred Feng
 * @since 2.0.1
 */
@Component
public class BeanLazyInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private final Observable lazyAutowiredObservable = Observable.unrepeatable();

	public void autowireLazily(final Object bean) {
		lazyAutowiredObservable.addObserver((ob, arg) -> {
			ApplicationContext applicationContext = (ApplicationContext) arg;
			AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
			beanFactory.autowireBean(bean);
		});
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		lazyAutowiredObservable.notifyObservers(event.getApplicationContext());
	}

}
