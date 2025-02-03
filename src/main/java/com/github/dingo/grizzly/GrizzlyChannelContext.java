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
