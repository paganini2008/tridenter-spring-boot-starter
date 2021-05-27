package indi.atlantis.framework.tridenter.http;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.github.paganini2008.devtools.collection.LruMap;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.MultiMappedMap;
import com.github.paganini2008.devtools.date.DateUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * DefaultRequestProcessor
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class DefaultRequestProcessor implements RequestProcessor {

	private static final int retryTemplateCacheSize = 256;

	private static final MultiMappedMap<String, String, RetryTemplate> retryTemplateCache = new MultiMappedMap<>(() -> {
		return new LruMap<String, RetryTemplate>(retryTemplateCacheSize);
	});

	private final RoutingAllocator routingAllocator;
	private final RestClientPerformer restClientPerformer;
	private final RetryTemplateFactory retryTemplateFactory;
	private final AsyncTaskExecutor taskExecutor;

	public DefaultRequestProcessor(RoutingAllocator routingAllocator, RestClientPerformer restClientPerformer,
			RetryTemplateFactory retryTemplateFactory, AsyncTaskExecutor taskExecutor) {
		this.routingAllocator = routingAllocator;
		this.restClientPerformer = restClientPerformer;
		this.retryTemplateFactory = retryTemplateFactory;
		this.taskExecutor = taskExecutor;
	}

	private final MultiValueMap<String, String> defaultHttpHeaders = new LinkedMultiValueMap<String, String>();

	public MultiValueMap<String, String> getDefaultHttpHeaders() {
		return defaultHttpHeaders;
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithRetry(String provider, Request request, Type responseType, int retries) {
		RetryTemplate retryTemplate = retryTemplateCache.get(provider, request.getPath(), () -> {
			return retryTemplateFactory.setRetryPolicy(retries).createObject();
		});
		RetryEntry retryEntry = new RetryEntry(provider, request, retries);
		return retryTemplate.execute(context -> {
			context.setAttribute(CURRENT_RETRY_IDENTIFIER, retryEntry);
			return sendRequest(provider, request, responseType);
		}, context -> {
			context.removeAttribute(CURRENT_RETRY_IDENTIFIER);
			Throwable e = context.getLastThrowable();
			throw RestClientUtils.wrapException(e.getMessage(), e, request);
		});

	}

	@Override
	public <T> ResponseEntity<T> sendRequest(String provider, Request request, Type responseType) {
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		String path = request.getPath();
		String url = routingAllocator.allocateHost(provider, path, request);
		if (request instanceof ParameterizedRequest) {
			ParameterizedRequest parameterizedRequest = (ParameterizedRequest) request;
			if (MapUtils.isNotEmpty(parameterizedRequest.getPathVariables())) {
				uriVariables.putAll(parameterizedRequest.getPathVariables());
			}
			if (MapUtils.isNotEmpty(parameterizedRequest.getRequestParameters())) {
				url = new StringBuilder(url).append("?").append(getQueryString(parameterizedRequest.getRequestParameters())).toString();
				uriVariables.putAll(parameterizedRequest.getRequestParameters());
			}
		}
		HttpEntity<?> body = request.getBody();
		if (MapUtils.isNotEmpty(defaultHttpHeaders)) {
			body.getHeaders().addAll(defaultHttpHeaders);
		}
		printFoot(url, request);
		return restClientPerformer.perform(url, request.getMethod(), body, responseType, uriVariables);
	}

	private String getQueryString(Map<String, Object> queryMap) {
		StringBuilder str = new StringBuilder();
		String[] names = queryMap.keySet().toArray(new String[0]);
		for (int i = 0, l = names.length; i < l; i++) {
			str.append(names[i]).append("={").append(names[i]).append("}");
			if (i != l - 1) {
				str.append("&");
			}
		}
		return str.toString();
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithTimeout(String provider, Request request, Type responseType, int timeout) {
		Future<ResponseEntity<T>> future = taskExecutor.submit(() -> {
			return sendRequest(provider, request, responseType);
		});
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (TimeoutException e) {
			throw new RestfulException(request, InterruptedType.REQUEST_TIMEOUT);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			throw RestClientUtils.wrapException(real.getMessage(), real, request);
		} catch (Throwable e) {
			throw RestClientUtils.wrapException(e.getMessage(), e, request);
		}
	}

	@Override
	public <T> ResponseEntity<T> sendRequestWithRetryAndTimeout(String provider, Request request, Type responseType, int retries,
			int timeout) {
		Future<ResponseEntity<T>> future = taskExecutor.submit(() -> {
			return sendRequestWithRetry(provider, request, responseType, retries);
		});
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (TimeoutException e) {
			throw new RestfulException(request, InterruptedType.REQUEST_TIMEOUT);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			throw RestClientUtils.wrapException(real.getMessage(), real, request);
		} catch (Throwable e) {
			throw RestClientUtils.wrapException(e.getMessage(), e, request);
		}
	}

	@Override
	public <T> T sendRequestWithRetry(String provider, Request request, RestTemplateCallback<T> callback, int retries) {
		RetryTemplate retryTemplate = retryTemplateCache.get(provider, request.getPath(), () -> {
			return retryTemplateFactory.setRetryPolicy(retries).createObject();
		});
		RetryEntry retryEntry = new RetryEntry(provider, request, retries);
		return retryTemplate.execute(context -> {
			context.setAttribute(CURRENT_RETRY_IDENTIFIER, retryEntry);
			return sendRequest(provider, request, callback);
		}, context -> {
			context.removeAttribute(CURRENT_RETRY_IDENTIFIER);
			Throwable e = context.getLastThrowable();
			throw RestClientUtils.wrapException(e.getMessage(), e, request);
		});
	}

	@Override
	public <T> T sendRequest(String provider, Request request, RestTemplateCallback<T> callback) {
		Map<String, Object> uriVariables = new HashMap<String, Object>();
		String path = request.getPath();
		String url = routingAllocator.allocateHost(provider, path, request);
		if (request instanceof ParameterizedRequest) {
			ParameterizedRequest parameterizedRequest = (ParameterizedRequest) request;
			if (MapUtils.isNotEmpty(parameterizedRequest.getPathVariables())) {
				uriVariables.putAll(parameterizedRequest.getPathVariables());
			}
			if (MapUtils.isNotEmpty(parameterizedRequest.getRequestParameters())) {
				url = new StringBuilder(url).append("?").append(getQueryString(parameterizedRequest.getRequestParameters())).toString();
				uriVariables.putAll(parameterizedRequest.getRequestParameters());
			}
		}
		HttpEntity<?> body = request.getBody();
		if (MapUtils.isNotEmpty(defaultHttpHeaders)) {
			body.getHeaders().addAll(defaultHttpHeaders);
		}
		printFoot(url, request);
		return restClientPerformer.perform(url, request.getMethod(), body, callback, uriVariables);
	}

	@Override
	public <T> T sendRequestWithTimeout(String provider, Request request, RestTemplateCallback<T> callback, int timeout) {
		Future<T> future = taskExecutor.submit(() -> {
			return sendRequest(provider, request, callback);
		});
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (TimeoutException e) {
			throw new RestfulException(request, InterruptedType.REQUEST_TIMEOUT);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			throw RestClientUtils.wrapException(real.getMessage(), real, request);
		} catch (Throwable e) {
			throw RestClientUtils.wrapException(e.getMessage(), e, request);
		}
	}

	@Override
	public <T> T sendRequestWithRetryAndTimeout(String provider, Request request, RestTemplateCallback<T> callback, int retries,
			int timeout) {
		Future<T> future = taskExecutor.submit(() -> {
			return sendRequestWithRetry(provider, request, callback, retries);
		});
		try {
			if (timeout > 0) {
				return future.get(timeout, TimeUnit.SECONDS);
			}
			return future.get();
		} catch (TimeoutException e) {
			throw new RestfulException(request, InterruptedType.REQUEST_TIMEOUT);
		} catch (ExecutionException e) {
			Throwable real = e.getCause();
			throw RestClientUtils.wrapException(real.getMessage(), real, request);
		} catch (Throwable e) {
			throw RestClientUtils.wrapException(e.getMessage(), e, request);
		}
	}

	private void printFoot(String url, Request request) {
		if (log.isTraceEnabled()) {
			log.trace("<RestClient path: {}>", url);
			log.trace("<RestClient use method: {}>", request.getMethod());
			log.trace("<RestClient request headers: {}>", request.getHeaders());
			log.trace("<RestClient date: {}>", DateUtils.format(request.getTimestamp(), "MM/dd/yy HH:mm:ss"));
			if (request instanceof ParameterizedRequest) {
				log.trace("<RestClient request parameters: {}>", ((ParameterizedRequest) request).getRequestParameters());
				log.trace("<RestClient path variables: {}>", ((ParameterizedRequest) request).getPathVariables());
			}
		}
	}

	@Getter
	@Setter
	static class RetryEntry {

		private String provider;
		private Request request;
		private int maxAttempts;

		RetryEntry(String provider, Request request, int maxAttempts) {
			this.provider = provider;
			this.request = request;
			this.maxAttempts = maxAttempts;
		}

	}
}
