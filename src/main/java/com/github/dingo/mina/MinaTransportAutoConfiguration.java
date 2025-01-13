package com.github.dingo.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
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
 * @Description: MinaTransportAutoConfiguration
 * @Author: Fred Feng
 * @Date: 13/01/2025
 * @Version 1.0.0
 */
@ConditionalOnClass({NioSocketConnector.class})
@ConditionalOnProperty(name = "doodler.transmitter.nio.selection", havingValue = "mina")
@Configuration(proxyBeanMethods = false)
public class MinaTransportAutoConfiguration {

    @Bean(initMethod = "open", destroyMethod = "close")
    public NioClient nioClient() {
        return new MinaClient();
    }

    @Bean
    public NioServer nioServer() {
        return new MinaServer();
    }

    @ConditionalOnMissingBean
    @Bean
    public ProtocolCodecFactory codecFactory(Serializer serializer) {
        return new MinaPacketCodecFactory(serializer);
    }

    @Bean
    public MinaServerHandler serverHandler() {
        return new MinaServerHandler();
    }

    @ConditionalOnMissingBean
    @Bean
    public ChannelEventListener<IoSession> channelEventListener() {
        return new MinaChannelEventListener();
    }

}
