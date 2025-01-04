package com.github.doodler.common.transmitter.rpc;

import com.github.doodler.common.context.ApplicationContextUtils;

/**
 * 
 * @Description: RPC
 * @Author: Fred Feng
 * @Date: 30/12/2024
 * @Version 1.0.0
 */
public abstract class RPC {

    public static <T> T createProxyInstance(Class<T> interfaceClass) throws Exception {
        return ApplicationContextUtils.autowireBean(new RpcProxyObjectFactoryBean<>(interfaceClass))
                .getObject();
    }

}
