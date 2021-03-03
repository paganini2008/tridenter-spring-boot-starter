package indi.atlantis.framework.tridenter.pool;

/**
 * 
 * Signature
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface Signature {

	String getBeanName();

	String getBeanClassName();

	String getMethodName();

	String getSuccessMethodName();

	String getFailureMethodName();

}