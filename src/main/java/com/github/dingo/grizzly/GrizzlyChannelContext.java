package com.github.dingo.grizzly;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.glassfish.grizzly.Connection;
import com.github.dingo.ChannelContext;
import com.github.dingo.Partitioner;
import com.google.common.base.Predicate;

/**
 * 
 * @Description: GrizzlyChannelContext
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public class GrizzlyChannelContext extends GrizzlyChannelContextSupport
        implements ChannelContext<Connection<?>> {

    private final List<Connection<?>> connectionCache = new CopyOnWriteArrayList<Connection<?>>();

    @Override
    public void addChannel(Connection<?> channel, int weight) {
        for (int i = 0; i < weight; i++) {
            connectionCache.add(channel);
        }
    }

    @Override
    public Connection<?> getChannel(SocketAddress address) {
        for (Connection<?> channel : connectionCache) {
            if (channel.getPeerAddress() != null && channel.getPeerAddress().equals(address)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public void removeChannel(SocketAddress address) {
        for (Connection<?> channel : connectionCache) {
            if (channel.getPeerAddress() != null && channel.getPeerAddress().equals(address)) {
                connectionCache.remove(channel);
            }
        }
    }

    @Override
    public int countOfChannels() {
        return connectionCache.size();
    }

    @Override
    public Connection<?> selectChannel(Object data, Partitioner partitioner) {
        return connectionCache.isEmpty() ? null : partitioner.selectChannel(data, connectionCache);
    }

    @Override
    public List<Connection<?>> getChannels(Predicate<SocketAddress> p) {
        return connectionCache.stream().filter(
                c -> c.getPeerAddress() != null && p.test((SocketAddress) c.getPeerAddress()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Connection<?>> getChannels() {
        return connectionCache;
    }

}
