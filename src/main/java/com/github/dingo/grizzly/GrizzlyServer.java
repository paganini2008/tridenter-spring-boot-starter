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
package com.github.dingo.grizzly;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.dingo.NioServer;
import com.github.dingo.TransmitterNioProperties;
import com.github.dingo.TransmitterServerException;
import com.github.doodler.common.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: GrizzlyServer
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class GrizzlyServer implements NioServer {

    private final AtomicBoolean started = new AtomicBoolean(false);
    private TCPNIOTransport transport;
    private DelayedExecutor delayedExecutor;
    private InetSocketAddress localAddress;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Autowired
    private GrizzlyServerHandler serverHandler;

    @Autowired
    private PacketCodecFactory codecFactory;

    @Override
    public SocketAddress start() {
        if (isStarted()) {
            throw new IllegalStateException("GrizzlyServer has been started.");
        }
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add(new TransportFilter());
        delayedExecutor = IdleTimeoutFilter.createDefaultIdleDelayedExecutor(5, TimeUnit.SECONDS);
        delayedExecutor.start();
        TransmitterNioProperties.NioServer serverConfig = nioProperties.getServer();
        IdleTimeoutFilter timeoutFilter =
                new IdleTimeoutFilter(delayedExecutor, serverConfig.getReaderIdleTimeout(),
                        TimeUnit.SECONDS, IdleTimeoutPolicies.READER_IDLE_LOG);
        filterChainBuilder.add(timeoutFilter);
        filterChainBuilder.add(new PacketFilter(codecFactory));
        filterChainBuilder.add(serverHandler);
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();

        final int nThreads = serverConfig.getThreadCount() > 0 ? serverConfig.getThreadCount()
                : Runtime.getRuntime().availableProcessors() * 2;
        ThreadPoolConfig tpConfig = ThreadPoolConfig.defaultConfig();
        tpConfig.setPoolName("GrizzlyServerHandler").setQueueLimit(-1).setCorePoolSize(nThreads)
                .setMaxPoolSize(nThreads).setKeepAliveTime(60L, TimeUnit.SECONDS);
        builder.setWorkerThreadPoolConfig(tpConfig);
        builder.setKeepAlive(true).setReuseAddress(true)
                .setReadBufferSize(serverConfig.getReaderBufferSize());
        builder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        builder.setServerConnectionBackLog(serverConfig.getBacklog());
        transport = builder.build();
        transport.setProcessor(filterChainBuilder.build());
        int port = NetUtils.getRandomPort(PORT_RANGE_BEGIN, PORT_RANGE_END);
        try {
            localAddress = StringUtils.isNotBlank(serverConfig.getBindHostName())
                    ? new InetSocketAddress(serverConfig.getBindHostName(), port)
                    : new InetSocketAddress(port);
            transport.bind(localAddress);
            transport.start();
            started.set(true);
            log.info("GrizzlyServer is started on: " + localAddress);
        } catch (Exception e) {
            throw new TransmitterServerException(e.getMessage(), e);
        }
        return localAddress;
    }

    @Override
    public void stop() {
        if (transport == null || !isStarted()) {
            return;
        }
        try {
            delayedExecutor.destroy();
            transport.shutdown(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        started.set(false);
        log.info("GrizzlyServer is closed successfully.");
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

}
