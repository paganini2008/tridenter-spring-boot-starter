package indi.atlantis.framework.tridenter.pool;

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