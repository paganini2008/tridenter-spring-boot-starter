package com.github.doodler.common.transmitter.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @Description: Transmitter
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcClient {

    String serviceId() default "";

    String className() default "";

    String beanName() default "";

    long timeout() default 60;

    int maxRetries() default 3;

    int retryInterval() default 1;

    Class<? extends Throwable>[] retryableExceptions() default Exception.class;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    Class<?> fallback() default void.class;

    Class<?> fallbackFactory() default void.class;

}
