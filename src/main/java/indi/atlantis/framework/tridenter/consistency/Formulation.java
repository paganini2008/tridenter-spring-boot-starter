package indi.atlantis.framework.tridenter.consistency;

/**
 * 
 * Formulation
 *
 * @author Fred Feng
 * @since 1.0
 */
public interface Formulation {

	static final String PREPARATION_PERIOD = "preparation:::";

	static final String COMMITMENT_PERIOD = "commitment:::";

	void directRun();

	boolean cancel();
	
	String getPeriod();

}
