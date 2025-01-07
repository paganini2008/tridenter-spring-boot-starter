package com.github.dingo.rpc;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.github.doodler.common.retry.RetryOperations;

/**
 * 
 * @Description: RpcProxyObjectFactoryBean
 * @Author: Fred Feng
 * @Date: 04/01/2025
 * @Version 1.0.0
 */
public class RpcProxyObjectFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> interfaceClass;

    public RpcProxyObjectFactoryBean(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Autowired
    private RpcTemplate rpcTemplate;

    @Autowired
    private RetryOperations retryOperations;

    @Autowired
    private ObjectProvider<RpcInterceptor> interceptors;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public T getObject() throws Exception {
        return new RpcProxyObject<>(interfaceClass, rpcTemplate, retryOperations, interceptors,
                applicationContext).getActualInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

}
