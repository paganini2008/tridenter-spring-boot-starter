package indi.atlantis.framework.seafloor.pool;

/**
 * 
 * InvocationBarrier
 *
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public final class InvocationBarrier {

	private final ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>() {

		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}

	};

	public void setCompleted() {
		threadLocal.set(Boolean.TRUE);
	}

	public boolean isCompleted() {
		boolean result = threadLocal.get();
		threadLocal.remove();
		return result;
	}

}
