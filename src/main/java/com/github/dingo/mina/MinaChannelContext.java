package com.github.dingo.mina;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.apache.mina.core.session.IoSession;
import com.github.dingo.ChannelContext;
import com.github.dingo.Partitioner;
import com.google.common.base.Predicate;

/**
 * 
 * @Description: MinaChannelContext
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public class MinaChannelContext extends MinaChannelContextSupport
        implements ChannelContext<IoSession> {

    private final List<IoSession> sessionCache = new CopyOnWriteArrayList<IoSession>();

    @Override
    public void addChannel(IoSession channel, int weight) {
        for (int i = 0; i < weight; i++) {
            sessionCache.add(channel);
        }
    }

    @Override
    public IoSession getChannel(SocketAddress address) {
        for (IoSession channel : sessionCache) {
            if (channel.getRemoteAddress() != null && channel.getRemoteAddress().equals(address)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public void removeChannel(SocketAddress address) {
        for (IoSession channel : sessionCache) {
            if (channel.getRemoteAddress() != null && channel.getRemoteAddress().equals(address)) {
                sessionCache.remove(channel);
            }
        }
    }

    @Override
    public int countOfChannels() {
        return sessionCache.size();
    }

    @Override
    public IoSession selectChannel(Object data, Partitioner partitioner) {
        return sessionCache.isEmpty() ? null : partitioner.selectChannel(data, sessionCache);
    }

    @Override
    public List<IoSession> getChannels() {
        return sessionCache;
    }

    @Override
    public List<IoSession> getChannels(Predicate<SocketAddress> p) {
        return sessionCache.stream()
                .filter(i -> i.getRemoteAddress() != null && p.test(i.getRemoteAddress()))
                .collect(Collectors.toUnmodifiableList());
    }

}
