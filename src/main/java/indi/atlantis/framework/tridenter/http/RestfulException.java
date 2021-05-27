package indi.atlantis.framework.tridenter.http;

import org.springframework.web.client.RestClientException;

/**
 * 
 * RestfulException
 *
 * @author Fred Feng
 * 
 * @since 1.0
 */
public class RestfulException extends RestClientException {

	private static final long serialVersionUID = -8762523199569525919L;

	public RestfulException(Request request, InterruptedType interruptedType) {
		this(request.toString(), request, interruptedType);
	}

	public RestfulException(String msg, Request request, InterruptedType interruptedType) {
		super(msg);
		this.request = request;
		this.interruptedType = interruptedType;
	}

	public RestfulException(String msg, Throwable e, Request request, InterruptedType interruptedType) {
		super(msg, e);
		this.request = request;
		this.interruptedType = interruptedType;
	}

	private final Request request;
	private final InterruptedType interruptedType;

	public Request getRequest() {
		return request;
	}

	public InterruptedType getInterruptedType() {
		return interruptedType;
	}

}
