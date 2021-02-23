package indi.atlantis.framework.seafloor.http;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

/**
 * 
 * DefaultRestClientPerformer
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class DefaultRestClientPerformer extends CharsetDefinedRestTemplate implements RestClientPerformer {

	public DefaultRestClientPerformer(Charset charset) {
		super(charset);
	}

	public DefaultRestClientPerformer(ClientHttpRequestFactory clientHttpRequestFactory, Charset charset) {
		super(clientHttpRequestFactory, charset);
	}

	@Override
	public <T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType, Object... uriVariables) {
		RequestCallback requestCallback = super.httpEntityCallback(requestBody, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = super.responseEntityExtractor(responseType);
		return super.execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> perform(String url, HttpMethod method, Object requestBody, Type responseType,
			Map<String, Object> uriParameters) {
		RequestCallback requestCallback = super.httpEntityCallback(requestBody, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = super.responseEntityExtractor(responseType);
		return super.execute(url, method, requestCallback, responseExtractor, uriParameters);
	}

	@Override
	public <T> T perform(String url, HttpMethod method, Object requestBody, ResponseExchanger<T> exchanger,
			Map<String, Object> uriParameters) {
		RequestCallback requestCallback = super.httpEntityCallback(requestBody, null);
		return super.execute(url, method, requestCallback, response -> {
			return exchanger.exchange(response);
		}, uriParameters);
	}

}
