package indi.atlantis.framework.tridenter.utils;

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
 * @version 1.0
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
