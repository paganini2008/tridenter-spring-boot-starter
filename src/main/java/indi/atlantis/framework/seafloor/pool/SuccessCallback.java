package indi.atlantis.framework.seafloor.pool;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * SuccessCallback
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class SuccessCallback extends Return implements Callback {

	private static final long serialVersionUID = 267993165180485661L;

	public SuccessCallback() {
	}

	SuccessCallback(Invocation invocation, Object returnValue) {
		super(invocation, returnValue);
	}

	@JsonIgnore
	public String getMethodName() {
		return getInvocation().getSignature().getSuccessMethodName();
	}

	@JsonIgnore
	public Object[] getArguments() {
		return new Object[] { getReturnValue(), getInvocation() };
	}
}
