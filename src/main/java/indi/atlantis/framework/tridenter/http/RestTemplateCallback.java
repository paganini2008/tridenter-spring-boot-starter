package indi.atlantis.framework.tridenter.http;

import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * RestTemplateCallback
 * 
 * @author Jimmy Hoff
 *
 * @version 1.0
 */
public interface RestTemplateCallback<T> {

	RequestCallback getRequestCallback(RestTemplate restTemplate);

	ResponseExtractor<T> getResponseExtractor(RestTemplate restTemplate);

}
