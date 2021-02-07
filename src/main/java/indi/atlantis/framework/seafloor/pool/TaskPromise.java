package indi.atlantis.framework.seafloor.pool;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 
 * TaskPromise
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface TaskPromise {

	Object get(Supplier<Object> defaultValue);

	Object get(long timeout, TimeUnit timeUnit, Supplier<Object> defaultValue);

	void cancel();

	boolean isCancelled();

	boolean isDone();

}
