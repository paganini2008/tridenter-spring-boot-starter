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
