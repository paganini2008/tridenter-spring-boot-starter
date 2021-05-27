package indi.atlantis.framework.tridenter.http;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * CharsetDefinedRestTemplate
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public class CharsetDefinedRestTemplate extends RestTemplate {

	public CharsetDefinedRestTemplate(Charset charset) {
		super();
		applySettings(charset);
	}

	public CharsetDefinedRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory, Charset charset) {
		super(clientHttpRequestFactory);
		applySettings(charset);
	}

	public CharsetDefinedRestTemplate(List<HttpMessageConverter<?>> messageConverters, Charset charset) {
		super(messageConverters);
		applySettings(charset);
	}

	private void applySettings(Charset charset) {
		final List<HttpMessageConverter<?>> messageConverters = getMessageConverters();
		for (int i = 0; i < messageConverters.size(); i++) {
			HttpMessageConverter<?> httpMessageConverter = messageConverters.get(i);
			if (httpMessageConverter instanceof StringHttpMessageConverter) {
				messageConverters.set(i, new StringHttpMessageConverter(charset));
			}
		}
	}

}
