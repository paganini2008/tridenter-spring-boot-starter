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
