package org.springtribe.framework.cluster.pool;

import java.util.Collection;

/**
 * 
 * DefaultParallelization
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class DefaultParallelization implements Parallelization {

	@Override
	public Object[] slice(Object argument) {
		if (argument instanceof CharSequence) {
			return ((CharSequence) argument).toString().split(",");
		} else if (argument instanceof Object[]) {
			return (Object[]) argument;
		} else if (argument instanceof Collection<?>) {
			return ((Collection<?>) argument).toArray();
		}
		return new Object[] { argument };
	}

	@Override
	public Object merge(Object[] results) {
		return results;
	}

}
