package com.github.doodler.common.transmitter;

import java.net.SocketAddress;
import java.util.List;
import com.google.common.base.Predicate;

/**
 * 
 * @Description: ChannelContext
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface ChannelContext<T> {

    default void addChannel(T channel) {
        addChannel(channel, 1);
    }

    void addChannel(T channel, int weight);

    T getChannel(SocketAddress address);

    List<T> getChannels(Predicate<SocketAddress> p);

    void removeChannel(SocketAddress address);

    int countOfChannels();

    T selectChannel(Object data, Partitioner partitioner);

    List<T> getChannels();

    void setChannelEventListener(ChannelEventListener<T> channelEventListener);

}
