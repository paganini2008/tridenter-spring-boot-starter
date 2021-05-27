package indi.atlantis.framework.tridenter.http;

import org.springframework.http.HttpStatus;

/**
 * 
 * InterruptedType
 *
 * @author Fred Feng
 * @version 1.0
 */
public enum InterruptedType {

	REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT), INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
	TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS);

	private final HttpStatus httpStatus;

	private InterruptedType(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

}
