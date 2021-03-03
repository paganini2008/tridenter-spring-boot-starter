package indi.atlantis.framework.tridenter.http;

import java.lang.reflect.Type;

import org.springframework.http.ResponseEntity;

/**
 * 
 * RequestProcessor
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface RequestProcessor {

	static final String CURRENT_RETRY_IDENTIFIER = "current-retry";

	<T> ResponseEntity<T> sendRequestWithRetry(String provider, Request request, Type responseType, int retries);

	<T> ResponseEntity<T> sendRequest(String provider, Request request, Type responseType);

	<T> ResponseEntity<T> sendRequestWithTimeout(String provider, Request request, Type responseType, int timeout);

	<T> ResponseEntity<T> sendRequestWithRetryAndTimeout(String provider, Request request, Type responseType, int retries, int timeout);

	<T> T sendRequestWithRetry(String provider, Request request, RestTemplateCallback<T> responseExchanger, int retries);

	<T> T sendRequest(String provider, Request request, RestTemplateCallback<T> responseExchanger);

	<T> T sendRequestWithTimeout(String provider, Request request, RestTemplateCallback<T> responseExchanger, int timeout);

	<T> T sendRequestWithRetryAndTimeout(String provider, Request request, RestTemplateCallback<T> responseExchanger, int retries,
			int timeout);
}
