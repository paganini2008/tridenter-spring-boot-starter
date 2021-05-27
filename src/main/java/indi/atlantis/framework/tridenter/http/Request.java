package indi.atlantis.framework.tridenter.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * 
 * Request
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public interface Request {

	String getPath();

	HttpMethod getMethod();

	HttpHeaders getHeaders();

	HttpEntity<Object> getBody();

	long getTimestamp();

}