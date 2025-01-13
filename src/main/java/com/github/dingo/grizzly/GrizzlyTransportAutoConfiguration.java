package com.github.dingo.grizzly;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.NioClient;
import com.github.dingo.NioServer;
import com.github.dingo.serializer.Serializer;

/**
 * 
 * @Description: GrizzlyTransportAutoConfiguration
 * @Author: Fred Feng
 * @Date: 13/01/2025
 * @Version 1.0.0
 */
@ConditionalOnClass({TCPNIOTransport.class})
@ConditionalOnProperty(name = "doodler.transmitter.nio.selection", havingValue = "grizzly")
@Configuration(proxyBeanMethods = false)
public class GrizzlyTransportAutoConfiguration {

    @Bean(initMethod = "open", destroyMethod = "close")
    public NioClient nioClient() {
        return new GrizzlyClient();
    }

    @Bean
    public NioServer nioServer() {
        return new GrizzlyServer();
    }

    @ConditionalOnMissingBean
    @Bean
    public PacketCodecFactory codecFactory(Serializer serializer) {
        return new GrizzlyPacketCodecFactory(serializer);
    }

    @Bean
    public GrizzlyServerHandler serverHandler() {
        return new GrizzlyServerHandler();
    }

    @ConditionalOnMissingBean
    @Bean
    public ChannelEventListener<Connection<?>> channelEventListener() {
        return new GrizzlyChannelEventListener();
    }
}
