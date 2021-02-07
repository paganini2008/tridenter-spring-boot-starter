package indi.atlantis.framework.seafloor.http;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

/**
 * 
 * RequestInterceptor
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface RequestInterceptor {

	default boolean beforeSubmit(String provider, Request request) {
		return true;
	}

	default void afterSubmit(String provider, Request request, @Nullable ResponseEntity<?> responseEntity, @Nullable Throwable reason) {
	}

	default boolean matches(String provider, Request request) {
		return true;
	}

}
