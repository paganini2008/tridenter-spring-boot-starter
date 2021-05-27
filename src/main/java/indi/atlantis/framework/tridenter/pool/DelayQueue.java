package indi.atlantis.framework.tridenter.pool;

/**
 * 
 * DelayQueue
 *
 * @author Fred Feng
 * @version 1.0
 */
public interface DelayQueue {

	void offer(Invocation invocation);

	Invocation pop();

	void waitForTermination();

	int size();

}