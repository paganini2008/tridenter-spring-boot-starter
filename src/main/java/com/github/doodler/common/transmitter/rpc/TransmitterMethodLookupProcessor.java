package com.github.doodler.common.transmitter.rpc;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 
 * @Description: TransmitterMethodLookupProcessor
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
public class TransmitterMethodLookupProcessor implements BeanPostProcessor {



    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        List<Method> methodList =
                MethodUtils.getMethodsListWithAnnotation(bean.getClass(), RpcClient.class);
        if (CollectionUtils.isEmpty(methodList)) {
            return bean;
        }

        return bean;
    }

}
