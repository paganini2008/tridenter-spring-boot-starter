package indi.atlantis.framework.seafloor.http;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.proxy.Aspect;

import indi.atlantis.framework.seafloor.ApplicationClusterContext;
import indi.atlantis.framework.seafloor.LeaderState;
import indi.atlantis.framework.seafloor.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * RestClientBeanAspect
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Slf4j
public class RestClientBeanAspect implements Aspect {

	private final String provider;
	private final RestClient restClient;
	private final Class<?> interfaceClass;
	private final ApplicationClusterContext applicationClusterContext;
	private final RequestTemplate requestTemplate;

	public RestClientBeanAspect(String provider, RestClient restClient, Class<?> interfaceClass,
			ApplicationClusterContext applicationClusterContext, RequestTemplate requestTemplate) {
		this.provider = provider;
		this.restClient = restClient;
		this.interfaceClass = interfaceClass;
		this.applicationClusterContext = applicationClusterContext;
		this.requestTemplate = requestTemplate;
	}

	@Override
	public boolean beforeCall(Object target, Method method, Object[] args) {
		if (applicationClusterContext.getLeaderState() == LeaderState.FATAL) {
			throw new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE, "RestClient is unavailable now");
		}
		return true;
	}

	@Override
	public Object call(Object proxy, Method method, Object[] args) throws Throwable {
		BasicRequest request;
		Api api = method.getAnnotation(Api.class);
		if (api != null) {
			request = buildDefaultRequest(api, method, args);
		} else if (method.getAnnotation(RequestMapping.class) != null) {
			request = new RequestSpringAnnotationSupported(method.getAnnotation(RequestMapping.class));
		} else {
			throw new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE, "No definition on target method");
		}
		request.setAttribute("methodSignature", new MethodSignature(interfaceClass, method, args));
		ResponseEntity<Object> responseEntity = requestTemplate.sendRequest(provider, request, method.getGenericReturnType());
		return responseEntity.getBody();
	}

	private BasicRequest buildDefaultRequest(Api api, Method method, Object[] args) {
		final String path = api.path();
		HttpMethod httpMethod = api.method();
		String[] headers = api.headers();
		ParameterizedRequestImpl request = new ParameterizedRequestImpl(path, httpMethod);
		request.getHeaders().setContentType(MediaType.parseMediaType(api.contentType()));
		if (ArrayUtils.isNotEmpty(headers)) {
			for (String header : headers) {
				String[] headerArgs = header.split("=", 2);
				if (headerArgs.length == 2) {
					request.getHeaders().add(headerArgs[0], headerArgs[1]);
				}
			}
		}
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			request.accessParameter(parameters[i], args[i]);
		}
		request.setTimeout(Integer.min(api.timeout(), restClient.timeout()));
		request.setRetries(Integer.max(api.retries(), restClient.retries()));
		request.setAllowedPermits(Integer.min(api.allowedPermits(), restClient.permits()));
		request.setFallback(getFallback(api.fallback(), restClient.fallback()));
		return request;
	}

	private FallbackProvider getFallback(Class<?> fallbackClass, Class<?> defaultFallbackClass) {
		try {
			if (fallbackClass != null && fallbackClass != Void.class && fallbackClass != void.class) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(fallbackClass);
			} else if (defaultFallbackClass != null && defaultFallbackClass != Void.class && defaultFallbackClass != void.class) {
				return (FallbackProvider) ApplicationContextUtils.getBeanIfNecessary(defaultFallbackClass);
			}
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void catchException(Object target, Method method, Object[] args, Throwable e) {
		if (e instanceof RestClientException) {
			throw (RestClientException) e;
		}
		log.error(e.getMessage(), e);
	}

	/**
	 * 
	 * MethodSignature
	 *
	 * @author Jimmy Hoff
	 * @version 1.0
	 */
	public static class MethodSignature {

		private final Class<?> interfaceClass;
		private final Method method;
		private final Object[] args;

		MethodSignature(Class<?> interfaceClass, Method method, Object[] args) {
			this.interfaceClass = interfaceClass;
			this.method = method;
			this.args = args;
		}

		public Class<?> getInterfaceClass() {
			return interfaceClass;
		}

		public Method getMethod() {
			return method;
		}

		public Object[] getArgs() {
			return args;
		}

	}

	/**
	 * 
	 * SpringAnnotationRequest
	 *
	 * @author Jimmy Hoff
	 * @version 1.0
	 */
	private static class RequestSpringAnnotationSupported extends ParameterizedRequestImpl {

		RequestSpringAnnotationSupported(RequestMapping requestMapping) {
			super(requestMapping.value()[0], HttpMethod.valueOf(requestMapping.method()[0].name()));
			if (ArrayUtils.isNotEmpty(requestMapping.produces())) {
				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
				for (String produce : requestMapping.produces()) {
					acceptableMediaTypes.add(MediaType.parseMediaType(produce));
				}
				getHeaders().setAccept(acceptableMediaTypes);
			}
		}

	}

}
