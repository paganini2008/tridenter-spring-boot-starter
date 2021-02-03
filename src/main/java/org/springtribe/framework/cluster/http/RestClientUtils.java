package org.springtribe.framework.cluster.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import com.github.paganini2008.devtools.StringUtils;

/**
 * 
 * RestClientUtils
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
public abstract class RestClientUtils {

	public static HttpStatus getHttpStatus(Throwable e) {
		return getHttpStatus(e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public static HttpStatus getHttpStatus(Throwable e, HttpStatus defaultHttpStatus) {
		if (e instanceof RestClientException) {
			if (e instanceof RestfulException) {
				return ((RestfulException) e).getInterruptedType().getHttpStatus();
			} else if (e instanceof HttpStatusCodeException) {
				return ((HttpStatusCodeException) e).getStatusCode();
			}
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
		return defaultHttpStatus;
	}

	public static ResponseEntity<String> getErrorResponse(Throwable e) {
		return getErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public static ResponseEntity<String> getErrorResponse(Throwable e, HttpStatus defaultHttpStatus) {
		HttpStatus httpStatus = defaultHttpStatus;
		if (e instanceof RestClientException) {
			if (e instanceof RestfulException) {
				httpStatus = ((RestfulException) e).getInterruptedType().getHttpStatus();
			} else if (e instanceof HttpStatusCodeException) {
				httpStatus = ((HttpStatusCodeException) e).getStatusCode();
			}
		}
		String msg = e.getMessage();
		if (StringUtils.isBlank(msg)) {
			msg = httpStatus.getReasonPhrase();
		}
		return new ResponseEntity<String>(msg, httpStatus);
	}

	public static RestfulException wrapException(String msg, Throwable e, Request request) {
		return wrapException(msg, e, request, InterruptedType.INTERNAL_SERVER_ERROR);
	}

	public static RestfulException wrapException(String msg, Throwable e, Request request, InterruptedType interruptedType) {
		if (e instanceof RestClientException) {
			throw (RestClientException) e;
		}
		throw new RestfulException(msg, e, request, interruptedType);
	}

}
