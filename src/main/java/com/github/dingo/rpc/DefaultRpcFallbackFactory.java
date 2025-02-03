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
