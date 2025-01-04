package com.github.doodler.common.transmitter.rpc;

import com.github.doodler.common.context.ApplicationContextUtils;
import lombok.experimental.UtilityClass;

/**
 * 
 * @Description: RPC
 * @Author: Fred Feng
 * @Date: 30/12/2024
 * @Version 1.0.0
 */
@UtilityClass
public final class RPC {

    public <T> T createProxyInstance(Class<T> interfaceClass) throws Exception {
        return ApplicationContextUtils.autowireBean(new RpcProxyObjectFactoryBean<>(interfaceClass))
                .getObject();
    }

}
