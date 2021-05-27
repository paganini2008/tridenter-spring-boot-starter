package indi.atlantis.framework.tridenter.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

/**
 * 
 * ForwardedRequest
 *
 * @author Fred Feng
 * @version 1.0
 */
public class ForwardedRequest extends BasicRequest implements Request {

	public ForwardedRequest(String path, String methodName) {
		this(path, HttpMethod.valueOf(methodName.toUpperCase()));
	}

	public ForwardedRequest(String path, HttpMethod method) {
		super(path, method, new HttpHeaders());
	}

	public ForwardedRequest(String path, HttpMethod method, HttpHeaders headers) {
		super(path, method, headers);
	}

	private int timeout = Integer.MAX_VALUE;
	private int retries;
	private int allowedPermits = Integer.MAX_VALUE;
	private FallbackProvider fallback;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public int getAllowedPermits() {
		return allowedPermits;
	}

	public void setAllowedPermits(int allowedPermits) {
		this.allowedPermits = allowedPermits;
	}

	public FallbackProvider getFallback() {
		return fallback;
	}

	public void setFallback(FallbackProvider fallback) {
		this.fallback = fallback;
	}

	public static ForwardedRequest getRequest(String path) {
		return new ForwardedRequest(path, HttpMethod.GET, new HttpHeaders());
	}

	public static ForwardedRequest postRequest(String path) {
		return new ForwardedRequest(path, HttpMethod.POST, new HttpHeaders());
	}

	public static ForwardedRequest putRequest(String path) {
		return new ForwardedRequest(path, HttpMethod.PUT, new HttpHeaders());
	}

	public static ForwardedRequest deleteRequest(String path) {
		return new ForwardedRequest(path, HttpMethod.DELETE, new HttpHeaders());
	}

}
