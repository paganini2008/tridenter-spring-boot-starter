package org.springtribe.framework.cluster.pool;

/**
 * 
 * Callback
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface Callback {
	
	Invocation getInvocation();

	String getMethodName();

	Object[] getArguments();

}
