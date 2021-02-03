package org.springtribe.framework.cluster.pool;

/**
 * 
 * Invocation
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface Invocation {

	String getId();

	Signature getSignature();

	Object[] getArguments();

	long getTimestamp();

}