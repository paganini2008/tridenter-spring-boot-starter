package indi.atlantis.framework.tridenter.pool;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * This annonation that decorate a method may represent to call in the whole
 * application cluster.
 *
 * @author Fred Feng
 * 
 * 
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiProcessing {
	
	String value();

	boolean async() default false;

	long timeout() default -1L;

	Class<? extends Throwable>[] ignoredFor() default Exception.class;

	String defaultValue() default "";
}
