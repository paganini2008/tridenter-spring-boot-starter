package indi.atlantis.framework.seafloor.http;

import static indi.atlantis.framework.seafloor.http.RequestProcessor.CURRENT_RETRY_IDENTIFIER;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import indi.atlantis.framework.seafloor.http.DefaultRequestProcessor.RetryEntry;

/**
 * 
 * RetryListenerContainer
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class RetryListenerContainer implements RetryListener {

	private final List<RetryListener> retryListeners = new CopyOnWriteArrayList<RetryListener>();
	private final List<ApiRetryListener> apiRetryListeners = new CopyOnWriteArrayList<ApiRetryListener>();

	public RetryListenerContainer() {
		retryListeners.add(this);
	}

	public void addListener(ApiRetryListener listener) {
		if (listener != null) {
			apiRetryListeners.add(listener);
		}
	}

	public void removeListener(ApiRetryListener listener) {
		if (listener != null) {
			apiRetryListeners.remove(listener);
		}
	}

	public void addListener(RetryListener listener) {
		if (listener != null) {
			retryListeners.add(listener);
		}
	}

	public void removeListener(RetryListener listener) {
		if (listener != null) {
			retryListeners.remove(listener);
		}
	}

	public List<RetryListener> getRetryListeners() {
		return retryListeners;
	}

	public List<ApiRetryListener> getApiRetryListeners() {
		return apiRetryListeners;
	}

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (context.hasAttribute(CURRENT_RETRY_IDENTIFIER)) {
			RetryEntry retryEntry = (RetryEntry) context.getAttribute(CURRENT_RETRY_IDENTIFIER);
			String provider = retryEntry.getProvider();
			Request request = retryEntry.getRequest();
			int retryCount = context.getRetryCount();
			apiRetryListeners.forEach(listener -> {
				if (listener.matches(provider, request)) {
					if (retryCount == 1) {
						listener.onRetryBegin(provider, request);
					}
					listener.onEachRetry(provider, request, throwable);
					
					if (retryCount == retryEntry.getMaxAttempts()) {
						listener.onRetryEnd(provider, request, throwable);
					}
				}
			});
		}
	}

}
