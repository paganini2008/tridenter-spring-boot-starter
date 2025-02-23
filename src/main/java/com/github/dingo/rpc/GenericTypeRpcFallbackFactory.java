/*
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dingo.rpc;

import java.lang.reflect.Method;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: GenericTypeRpcFallbackFactory
 * @Author: Fred Feng
 * @Date: 04/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class GenericTypeRpcFallbackFactory<T> implements RpcFallbackFactory<T>, MethodInterceptor {

    private final Class<T> apiInterfaceClass;

    public GenericTypeRpcFallbackFactory(Class<T> apiInterfaceClass) {
        this.apiInterfaceClass = apiInterfaceClass;
    }

    private T rpcClientProxy;

    @Override
    public T getFallback(Throwable e) {
        if (e != null) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        if (rpcClientProxy == null) {
            rpcClientProxy = createProxyObject();
        }
        return rpcClientProxy;
    }

    @SuppressWarnings("unchecked")
    private T createProxyObject() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(apiInterfaceClass);
        enhancer.setCallback(this);
        return (T) enhancer.create();
    }

    @Override
    public final Object intercept(Object proxy, Method method, Object[] args,
            MethodProxy methodProxy) throws Throwable {
        final String methodName = method.getName();
        if (methodName.equals("equals")) {
            return false;
        } else if (methodName.equals("hashcode")) {
            return System.identityHashCode(this);
        } else if (methodName.equals("toString")) {
            return super.toString();
        }
        return invokeNullableMethod(apiInterfaceClass, proxy, method, args);
    }

    protected Object invokeNullableMethod(Class<?> apiInterfaceClass, Object proxy, Method method,
            Object[] args) {
        return null;
    }
}
