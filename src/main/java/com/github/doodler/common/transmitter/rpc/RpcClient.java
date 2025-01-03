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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcClient {

    String serviceId() default "";

    String className() default "";

    String beanName() default "";

    long timeout() default 60;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    int maxRetries() default 3;

    Class<?> fallback() default Void.class;

    Class<?> fallbackFactory() default Void.class;

}
