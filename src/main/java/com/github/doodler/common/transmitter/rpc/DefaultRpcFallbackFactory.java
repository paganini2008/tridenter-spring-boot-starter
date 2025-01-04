package com.github.doodler.common.transmitter.rpc;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.context.ApplicationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: DefaultRpcFallbackFactory
 * @Author: Fred Feng
 * @Date: 04/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class DefaultRpcFallbackFactory<T> implements RpcFallbackFactory<T> {

    private final Class<T> fallbackClass;
    private final ApplicationContext applicationContext;

    DefaultRpcFallbackFactory(Class<T> fallbackClass, ApplicationContext applicationContext) {
        this.fallbackClass = fallbackClass;
        this.applicationContext = applicationContext;
    }

    @Override
    public T getFallback(Throwable cause) {
        try {
            return (T) applicationContext.getBean(fallbackClass);
        } catch (RuntimeException e) {
            try {
                return (T) ConstructorUtils.invokeConstructor(fallbackClass);
            } catch (Exception ee) {
                if (log.isErrorEnabled()) {
                    log.error(ee.getMessage(), ee);
                }
                return null;
            }
        }
    }
}
