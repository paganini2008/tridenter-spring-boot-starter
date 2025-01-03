package com.github.doodler.common.transmitter.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @Description: RpcProxyObject
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public class RpcProxyObject<T> implements InvocationHandler {

    private final Class<T> interfaceClass;
    private final String serviceId;
    private final String className;
    private final String beanName;
    private final int maxRetries;
    private final long timeout;
    private final TimeUnit timeUnit;
    private final Object actualInstance;
    private final RpcTemplate rpcTemplate;

    RpcProxyObject(Class<T> interfaceClass, RpcTemplate rpcTemplate) {
        RpcClient rpcClient = interfaceClass.getAnnotation(RpcClient.class);
        this.interfaceClass = interfaceClass;
        this.serviceId = rpcClient.serviceId();
        this.className = rpcClient.className();
        this.beanName = rpcClient.beanName();
        this.maxRetries = rpcClient.maxRetries();
        this.timeout = rpcClient.timeout();
        this.timeUnit = rpcClient.timeUnit();
        this.rpcTemplate = rpcTemplate;
        this.actualInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {interfaceClass}, this);
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
