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

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.dingo.NioClient;
import com.github.dingo.Partitioner;
import com.github.doodler.common.context.BeanReflectionService;

/**
 * 
 * @Description: RpcAutoConfiguration
 * @Author: Fred Feng
 * @Date: 04/01/2025
 * @Version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class RpcAutoConfiguration {

    @Bean
    public RpcTemplate rpcTemplate(NioClient nioClient, Partitioner partitioner,
            DiscoveryClient discoveryClient) {
        return new RpcTemplate(nioClient, partitioner, discoveryClient);
    }

    @Bean
    public ExpressionInvocationPacketHandler expressionInvocationPacketHandler() {
        return new ExpressionInvocationPacketHandler();
    }

    @Bean
    public MethodInvocationPacketHandler methodInvocationPacketHandler(
            BeanReflectionService beanReflectionService) {
        return new MethodInvocationPacketHandler(beanReflectionService);
    }

}
