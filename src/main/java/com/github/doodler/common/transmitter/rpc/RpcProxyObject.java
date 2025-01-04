package com.github.doodler.common.transmitter.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.context.ApplicationContext;
import com.github.doodler.common.retry.RetryOperations;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: RpcProxyObject
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class RpcProxyObject<T> implements InvocationHandler {

    private final Class<T> interfaceClass;
    private final String serviceId;
    private final String className;
    private final String beanName;
    private final int maxRetries;
    private final long timeout;
    private final TimeUnit timeUnit;
    private final Supplier<RpcFallbackFactory<T>> fallbackFactorySupplier;
    private final Object actualInstance;
    private final RpcTemplate rpcTemplate;
    private final RetryOperations retryOperations;
    private final ApplicationContext applicationContext;


    RpcProxyObject(Class<T> interfaceClass, RpcTemplate rpcTemplate,
            RetryOperations retryOperations, ApplicationContext applicationContext) {
        RpcClient rpcClient = interfaceClass.getAnnotation(RpcClient.class);
        this.interfaceClass = interfaceClass;
        this.serviceId = rpcClient.serviceId();
        this.className = rpcClient.className();
        this.beanName = rpcClient.beanName();
        this.maxRetries = rpcClient.maxRetries();
        this.timeout = rpcClient.timeout();
        this.timeUnit = rpcClient.timeUnit();
        this.fallbackFactorySupplier =
                getFallbackFactorySupplier(rpcClient.fallbackFactory(), rpcClient.fallback());
        this.rpcTemplate = rpcTemplate;
        this.retryOperations = retryOperations;
        this.applicationContext = applicationContext;
        this.actualInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {interfaceClass}, this);
    }

    private Supplier<RpcFallbackFactory<T>> getFallbackFactorySupplier(
            Class<?> fallbackFactoryClass, Class<?> fallbackClass) {
        Supplier<RpcFallbackFactory<T>> fallbackFactorySupplier = null;
        if (fallbackFactoryClass != null && !fallbackFactoryClass.equals(void.class)) {
            if (GenericTypeRpcFallbackFactory.class.isAssignableFrom(fallbackFactoryClass)) {
                fallbackFactorySupplier = () -> {
                    return getFallbackFactory(fallbackFactoryClass, interfaceClass);
                };
            } else {
                fallbackFactorySupplier = () -> {
                    return getFallbackFactory(fallbackFactoryClass);
                };
            }
        } else if (fallbackClass != null && !fallbackClass.equals(void.class)) {
            fallbackFactorySupplier = () -> {
                return new DefaultRpcFallbackFactory<>((Class<T>) fallbackClass,
                        applicationContext);
            };
        }
        return fallbackFactorySupplier;
    }

    @SuppressWarnings("unchecked")
    private <F extends RpcFallbackFactory<T>> F getFallbackFactory(Class<?> fallbackFactoryClass,
            Object... args) {
        try {
            return (F) applicationContext.getBean(fallbackFactoryClass);
        } catch (RuntimeException e) {
            try {
                if (ArrayUtils.isNotEmpty(args)) {
                    return (F) ConstructorUtils.invokeConstructor(fallbackFactoryClass, args);
                }
                return (F) ConstructorUtils.invokeConstructor(fallbackFactoryClass);
            } catch (Exception ee) {
                if (log.isErrorEnabled()) {
                    log.error(ee.getMessage(), ee);
                }
                return null;
            }
        }
    }

    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    public T getActualInstance() {
        return interfaceClass.cast(actualInstance);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (StringUtils.isNotBlank(serviceId)) {
            return rpcTemplate.invokeTargetMethod(serviceId, className, beanName, method.getName(),
                    args, timeout, timeUnit);
        }
        return rpcTemplate.invokeTargetMethod(className, beanName, method.getName(), args, timeout,
                timeUnit);
    }

}
