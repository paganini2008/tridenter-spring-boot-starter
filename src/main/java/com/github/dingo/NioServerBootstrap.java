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
package com.github.dingo;

import java.net.InetSocketAddress;
import java.util.Map;
import org.springframework.core.Ordered;
import com.github.doodler.common.cloud.MetadataCollector;
import com.github.doodler.common.context.ManagedBeanLifeCycle;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: NioServerBootstrap
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class NioServerBootstrap implements ManagedBeanLifeCycle, MetadataCollector, Ordered {

    private final NioServer nioServer;

    private InetSocketAddress localAddress;

    @Override
    public void afterPropertiesSet() throws Exception {
        localAddress = (InetSocketAddress) nioServer.start();
    }

    @Override
    public Map<String, String> getInitialData() {
        if (localAddress == null) {
            throw new IllegalStateException("NioServer is not started.");
        }
        String serverLocation = localAddress.getHostString() + ":" + localAddress.getPort();
        return Map.of(TransmitterConstants.TRANSMITTER_SERVER_LOCATION, serverLocation);
    }

    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public void destroy() throws Exception {
        if (nioServer != null) {
            nioServer.stop();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }


}
