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
package com.github.dingo.mina;

import java.util.concurrent.TimeUnit;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ChannelSwitcher;
import com.github.dingo.NioClient;
import com.github.dingo.NioServer;
import com.github.dingo.TransmitterNioProperties;
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

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Bean(initMethod = "open", destroyMethod = "close")
    public NioClient nioClient() {
        MinaClient minaClient = new MinaClient();
        minaClient.watchConnection(nioProperties.getClient().getReconnectInterval(),
                TimeUnit.SECONDS, nioProperties.getClient().getMaxReconnectAttempts());
        return minaClient;
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

    @Bean
    public ChannelEventListener<IoSession> channelEventListener() {
        return new MinaChannelEventListener();
    }

    @Bean
    public ChannelEventListener<IoSession> cleanChannelEventListener(ChannelSwitcher channelSwitch) {
        return new CleanChannelEventListener(channelSwitch);
    }

}
