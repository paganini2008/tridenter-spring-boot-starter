package indi.atlantis.framework.tridenter.http;

import java.lang.reflect.Type;

import org.springframework.web.client.RestClientException;

/**
 * 
 * DefaultFallbackProvider
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public class DefaultFallbackProvider implements FallbackProvider {

	@Override
	public Object getBody(String provider, Request request, Type responseType, RestClientException e) {
		return null;
	}

}
