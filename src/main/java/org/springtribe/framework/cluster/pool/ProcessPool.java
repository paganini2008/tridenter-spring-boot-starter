package org.springtribe.framework.cluster.pool;

/**
 * 
 * ProcessPool
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface ProcessPool {

	void execute(Invocation invocation);

	TaskPromise submit(Invocation invocation);

	int getQueueSize();

	void shutdown();

}
