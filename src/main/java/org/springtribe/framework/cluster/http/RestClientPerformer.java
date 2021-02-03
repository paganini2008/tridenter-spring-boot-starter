package org.springtribe.framework.cluster.http;

import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * 
 * RestClientPerformer
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public interface RestClientPerformer {

	<T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType, Object... uriVariables);

	<T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType, Map<String, Object> uriParameters);

}