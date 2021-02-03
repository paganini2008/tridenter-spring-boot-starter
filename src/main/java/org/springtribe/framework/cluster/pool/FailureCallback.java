package org.springtribe.framework.cluster.pool;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * FailureCallback
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Setter
@Getter
public class FailureCallback extends Return implements Callback {

	private static final long serialVersionUID = 4923243066426434433L;

	public FailureCallback() {
	}

	private ThrowableProxy throwableProxy;

	FailureCallback(Invocation invocation, Throwable reason) {
		super(invocation, null);
		this.throwableProxy = new ThrowableProxy(reason.getMessage(), reason);
	}

	@JsonIgnore
	public String getMethodName() {
		return getInvocation().getSignature().getFailureMethodName();
	}

	@JsonIgnore
	public Object[] getArguments() {
		return new Object[] { throwableProxy, getInvocation() };
	}

}
