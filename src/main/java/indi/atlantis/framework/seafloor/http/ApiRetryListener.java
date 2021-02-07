package indi.atlantis.framework.seafloor.http;

/**
 * 
 * ApiRetryHandler
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface ApiRetryListener {

	default void onRetryBegin(String provider, Request request) {
	}

	default void onRetryEnd(String provider, Request request, Throwable e) {
	}

	default void onEachRetry(String provider, Request request, Throwable e) {
	}

	default boolean matches(String provider, Request request) {
		return true;
	}

}
