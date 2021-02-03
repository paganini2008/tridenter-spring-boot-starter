package org.springtribe.framework.cluster.http;

import org.springframework.web.client.RestClientException;

/**
 * 
 * RoutingPolicyException
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class RoutingPolicyException extends RestClientException {

	private static final long serialVersionUID = -7527134536561359418L;

	public RoutingPolicyException(String msg) {
		super(msg);
	}

	public RoutingPolicyException(String msg, Throwable e) {
		super(msg, e);
	}

}
