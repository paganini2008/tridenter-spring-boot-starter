package com.github.dingo.rpc;

import java.lang.reflect.Method;

/**
 * 
 * @Description: RpcInterceptor
 * @Author: Fred Feng
 * @Date: 05/01/2025
 * @Version 1.0.0
 */
public interface RpcInterceptor {

    default void beforeInvocation(Object handler, Method method, Object[] args) {}

    default void afterInvocation(Object handler, Method method, Object[] args, Throwable cause) {}

}
