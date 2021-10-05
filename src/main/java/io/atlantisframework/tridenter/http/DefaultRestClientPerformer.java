/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.atlantisframework.tridenter.http;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * DefaultRestClientPerformer
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class DefaultRestClientPerformer extends CharsetDefinedRestTemplate implements RestClientPerformer {

	public DefaultRestClientPerformer(Charset charset) {
		super(charset);
	}

	public DefaultRestClientPerformer(ClientHttpRequestFactory clientHttpRequestFactory, Charset charset) {
		super(clientHttpRequestFactory, charset);
	}

	public DefaultRestClientPerformer(List<HttpMessageConverter<?>> messageConverters, Charset charset) {
		super(messageConverters, charset);
	}

	@Override
	public <T> ResponseEntity<T> perform(String url, HttpMethod method, final Object requestBody, final Type responseType,
			Object... uriVariables) {
		return perform(url, method, requestBody, new RestTemplateCallback<ResponseEntity<T>>() {

			@Override
			public RequestCallback getRequestCallback(RestTemplate restTemplate) {
				return restTemplate.httpEntityCallback(requestBody, responseType);
			}

			@Override
			public ResponseExtractor<ResponseEntity<T>> getResponseExtractor(RestTemplate restTemplate) {
				return restTemplate.responseEntityExtractor(responseType);
			}

		}, uriVariables);
	}

	@Override
	public <T> ResponseEntity<T> perform(String url, HttpMethod method, final Object requestBody, final Type responseType,
			Map<String, Object> uriParameters) {
		return perform(url, method, requestBody, new RestTemplateCallback<ResponseEntity<T>>() {

			@Override
			public RequestCallback getRequestCallback(RestTemplate restTemplate) {
				return restTemplate.httpEntityCallback(requestBody, responseType);
			}

			@Override
			public ResponseExtractor<ResponseEntity<T>> getResponseExtractor(RestTemplate restTemplate) {
				return restTemplate.responseEntityExtractor(responseType);
			}

		}, uriParameters);
	}

	@Override
	public <T> T perform(String url, HttpMethod method, Object requestBody, RestTemplateCallback<T> callback, Object... uriVariables) {
		RequestCallback requestCallback = callback.getRequestCallback(this);
		ResponseExtractor<T> responseExtractor = callback.getResponseExtractor(this);
		return super.execute(url, method, requestCallback, responseExtractor, uriVariables);
	}

	@Override
	public <T> T perform(String url, HttpMethod method, Object requestBody, RestTemplateCallback<T> callback,
			Map<String, Object> uriParameters) {
		RequestCallback requestCallback = callback.getRequestCallback(this);
		ResponseExtractor<T> responseExtractor = callback.getResponseExtractor(this);
		return super.execute(url, method, requestCallback, responseExtractor, uriParameters);
	}

}
