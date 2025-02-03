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
package com.github.dingo.netty;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.MessageCodecFactory;
import com.github.dingo.NioClient;
import com.github.dingo.NioServer;
import com.github.dingo.TransmitterNioProperties;
import com.github.dingo.serializer.Serializer;
import io.netty.channel.Channel;

/**
 * 
 * @Description: NettyTransportAutoConfiguration
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "doodler.transmitter.nio.selection", havingValue = "netty",
        matchIfMissing = true)
public class NettyTransportAutoConfiguration {

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Bean(initMethod = "open", destroyMethod = "close")
    public NioClient nioClient() {
        NettyClient nettyClient = new NettyClient();
        nettyClient.watchConnection(nioProperties.getClient().getReconnectInterval(),
                TimeUnit.SECONDS, nioProperties.getClient().getMaxReconnectAttempts());
        return nettyClient;
    }

    @Bean
    public NioServer nioServer() {
        return new NettyServer();
    }

    @ConditionalOnMissingBean
    @Bean
    public KeepAlivePolicy keepAlivePolicy() {
        return new NettyServerKeepAlivePolicy();
    }

    @Bean
    public MessageCodecFactory codecFactory(Serializer serializer) {
        return new NettyMessageCodecFactory(serializer);
    }

    @Bean
    public NettyServerHandler serverHandler() {
        return new NettyServerHandler();
    }

    @Bean
    public ChannelEventListener<Channel> channelEventListener() {
        return new NettyChannelEventListener();
    }
}
