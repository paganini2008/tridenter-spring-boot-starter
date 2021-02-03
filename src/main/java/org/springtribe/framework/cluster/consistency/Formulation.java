package org.springtribe.framework.cluster.consistency;

/**
 * 
 * Formulation
 *
 * @author Jimmy Hoff
 * @since 1.0
 */
public interface Formulation {

	static final String PREPARATION_PERIOD = "preparation:::";

	static final String COMMITMENT_PERIOD = "commitment:::";

	void directRun();

	boolean cancel();
	
	String getPeriod();

}
