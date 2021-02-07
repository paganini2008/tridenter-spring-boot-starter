package indi.atlantis.framework.seafloor.http;

import static indi.atlantis.framework.seafloor.http.RequestProcessor.CURRENT_RETRY_IDENTIFIER;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

import indi.atlantis.framework.seafloor.http.DefaultRequestProcessor.RetryEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * LoggingRetryListener
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class LoggingRetryListener implements RetryListener {

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		if (log.isTraceEnabled()) {
			if (context.hasAttribute(CURRENT_RETRY_IDENTIFIER)) {
				RetryEntry retryEntry = (RetryEntry) context.getAttribute(CURRENT_RETRY_IDENTIFIER);
				String provider = retryEntry.getProvider();
				Request request = retryEntry.getRequest();
				log.trace("[{}] Retry: {}, Times: {}/{}", provider, request, context.getRetryCount(), retryEntry.getMaxAttempts());
			}
		}
	}

}
