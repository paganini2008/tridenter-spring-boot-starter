package com.github.dingo.netty;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import com.github.dingo.ChannelContext;
import com.github.dingo.Partitioner;
import com.google.common.base.Predicate;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * 
 * @Description: NettyChannelContext
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
@Sharable
public class NettyChannelContext extends NettyChannelContextSupport
        implements ChannelContext<Channel> {

    private final List<Channel> channelCache = new CopyOnWriteArrayList<Channel>();

    @Override
    public void addChannel(Channel channel, int weight) {
        for (int i = 0; i < weight; i++) {
            channelCache.add(channel);
        }
    }

    @Override
    public Channel getChannel(SocketAddress address) {
        for (Channel channel : channelCache) {
            if (channel.remoteAddress() != null && channel.remoteAddress().equals(address)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Channel> getChannels(Predicate<SocketAddress> p) {
        return channelCache.stream()
                .filter(c -> c.remoteAddress() != null && p.test(c.remoteAddress()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void removeChannel(SocketAddress address) {
        for (Channel channel : channelCache) {
            if (channel.remoteAddress() != null && channel.remoteAddress().equals(address)) {
                channelCache.remove(channel);
            }
        }
    }

    @Override
    public int countOfChannels() {
        return channelCache.size();
    }

    @Override
    public Channel selectChannel(Object data, Partitioner partitioner) {
        return channelCache.isEmpty() ? null : partitioner.selectChannel(data, channelCache);
    }

    @Override
    public List<Channel> getChannels() {
        return channelCache;
    }

}
