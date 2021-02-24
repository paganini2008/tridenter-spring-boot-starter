package indi.atlantis.framework.seafloor.http;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * RequestInterceptorContainer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public class RequestInterceptorContainer implements BeanPostProcessor {

	private final List<RequestInterceptor> interceptors = new CopyOnWriteArrayList<RequestInterceptor>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RequestInterceptor) {
			addInterceptor((RequestInterceptor) bean);
		}
		return bean;
	}

	public void addInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.add(interceptor);
		}
	}

	public void removeInterceptor(RequestInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.remove(interceptor);
		}
	}

	public boolean beforeSubmit(String provider, Request request) {
		boolean proceeded = true;
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				proceeded &= interceptor.beforeSubmit(provider, request);
			}
		}
		return proceeded;
	}

	public void afterSubmit(String provider, Request request, Object responseEntity, Throwable reason) {
		for (RequestInterceptor interceptor : interceptors) {
			if (interceptor.matches(provider, request)) {
				interceptor.afterSubmit(provider, request, responseEntity, reason);
			}
		}
	}

}
