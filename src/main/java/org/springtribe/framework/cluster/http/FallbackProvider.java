package org.springtribe.framework.cluster.http;

import java.lang.reflect.Type;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestClientException;

/**
 * 
 * FallbackProvider
 *
 * @author Jimmy Hoff
 * 
 * @since 1.0
 */
public interface FallbackProvider {

	default HttpStatus getHttpStatus() {
		return HttpStatus.OK;
	}

	default HttpHeaders getHeaders() {
		return new HttpHeaders();
	}

	default boolean hasFallback(String provider, Request request, Type responseType, @Nullable RestClientException e) {
		return true;
	}

	Object getBody(String provider, Request request, Type responseType, @Nullable RestClientException e);

}
