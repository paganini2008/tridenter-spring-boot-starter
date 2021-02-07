package indi.atlantis.framework.seafloor.http;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 
 * RetryTemplateFactory
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class RetryTemplateFactory implements BeanPostProcessor {

	private final RetryListenerContainer retryListenerContainer = new RetryListenerContainer();
	private RetryPolicy retryPolicy = new SimpleRetryPolicy();
	private BackOffPolicy backOffPolicy = new FixedBackOffPolicy();

	public RetryTemplateFactory setRetryPolicy(int maxAttempts) {
		this.retryPolicy = maxAttempts > 0 ? new SimpleRetryPolicy(maxAttempts) : new NeverRetryPolicy();
		return this;
	}

	public RetryTemplateFactory setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
		return this;
	}

	public RetryTemplateFactory setBackOffPolicy(BackOffPolicy backOffPolicy) {
		this.backOffPolicy = backOffPolicy;
		return this;
	}

	public RetryTemplateFactory setBackOffPeriod(long backOffPeriod) {
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(backOffPeriod);
		this.backOffPolicy = backOffPolicy;
		return this;
	}

	public RetryTemplateFactory addListener(RetryListener retryListener) {
		if (retryListener != null) {
			this.retryListenerContainer.addListener(retryListener);
		}
		return this;
	}

	public RetryTemplateFactory removeListener(RetryListener retryListener) {
		if (retryListener != null) {
			this.retryListenerContainer.removeListener(retryListener);
		}
		return this;
	}

	public RetryTemplateFactory addListener(ApiRetryListener retryListener) {
		if (retryListener != null) {
			this.retryListenerContainer.addListener(retryListener);
		}
		return this;
	}

	public RetryTemplateFactory removeListener(ApiRetryListener retryListener) {
		if (retryListener != null) {
			this.retryListenerContainer.removeListener(retryListener);
		}
		return this;
	}

	public RetryTemplate createObject() {
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		retryTemplate.setListeners(this.retryListenerContainer.getRetryListeners().toArray(new RetryListener[0]));
		return retryTemplate;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RetryListener) {
			addListener((RetryListener) bean);
		} else if (bean instanceof ApiRetryListener) {
			addListener((ApiRetryListener) bean);
		}
		return bean;
	}

}
