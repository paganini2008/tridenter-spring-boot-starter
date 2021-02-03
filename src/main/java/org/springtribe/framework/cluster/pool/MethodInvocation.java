package org.springtribe.framework.cluster.pool;

import java.io.Serializable;
import java.util.UUID;

import com.github.paganini2008.devtools.beans.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * MethodInvocation
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
@Getter
@Setter
public class MethodInvocation implements Serializable, Invocation {

	private static final long serialVersionUID = -5401293046063974728L;

	private String id;
	private Signature signature;
	private Object[] arguments;
	private long timestamp;

	MethodInvocation(Signature signature, Object... arguments) {
		this.id = UUID.randomUUID().toString();
		this.signature = signature;
		this.arguments = arguments;
		this.timestamp = System.currentTimeMillis();
	}

	public MethodInvocation() {
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
