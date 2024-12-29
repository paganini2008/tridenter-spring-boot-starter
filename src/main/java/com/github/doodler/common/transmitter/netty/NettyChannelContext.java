package com.github.doodler.common.transmitter.netty;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.github.doodler.common.transmitter.ChannelContext;
import com.github.doodler.common.transmitter.Partitioner;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class NettyChannelContext extends NettyChannelContextAware
        implements ChannelContext<Channel> {

    private final List<Channel> channelHolds = new CopyOnWriteArrayList<Channel>();

    public void addChannel(Channel channel, int weight) {
        for (int i = 0; i < weight; i++) {
            channelHolds.add(channel);
        }
        if (log.isTraceEnabled()) {
            log.trace("Current channel size: " + countOfChannels());
        }
    }

    public Channel getChannel(SocketAddress address) {
        for (Channel channel : channelHolds) {
            if (channel.remoteAddress() != null && channel.remoteAddress().equals(address)) {
                return channel;
            }
        }
        return null;
    }

    public void removeChannel(SocketAddress address) {
        for (Channel channel : channelHolds) {
            if (channel.remoteAddress() != null && channel.remoteAddress().equals(address)) {
                channelHolds.remove(channel);
            }
        }
    }

    public int countOfChannels() {
        return channelHolds.size();
    }

    public Channel selectChannel(Object data, Partitioner partitioner) {
        return channelHolds.isEmpty() ? null : partitioner.selectChannel(data, channelHolds);
    }

    public Collection<Channel> getChannels() {
        return channelHolds;
    }

}
