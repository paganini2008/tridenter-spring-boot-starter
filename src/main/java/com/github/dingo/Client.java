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

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import com.github.doodler.common.utils.NetUtils;

/**
 * 
 * @Description: Client
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface Client {

    void send(Object data);

    void send(Object data, SocketAddress socketAddress);

    void send(Object data, Partitioner partitioner);

    default void send(Object data, String serviceLocation) {
        send(data, NetUtils.parse(serviceLocation));
    }

    default Object sendAndReturn(Object data, String serviceLocation) {
        return sendAndReturn(data, NetUtils.parse(serviceLocation));
    }

    default Object sendAndReturn(Object data, String serviceLocation, long timeout,
            TimeUnit timeUnit) {
        return sendAndReturn(data, NetUtils.parse(serviceLocation), timeout, timeUnit);
    }

    Object sendAndReturn(Object data, SelectedChannelCallback callback);

    Object sendAndReturn(Object data, SelectedChannelCallback callback, long timeout,
            TimeUnit timeUnit);

    default Object sendAndReturn(Object data, SocketAddress address) {
        return sendAndReturn(data, new SelectedChannelCallback() {
            @Override
            public <T> T doSelectChannel(ChannelContext<T> channelContext) {
                return channelContext.getChannel(address);
            }
        });
    }

    default Object sendAndReturn(Object data, SocketAddress address, long timeout,
            TimeUnit timeUnit) {
        return sendAndReturn(data, new SelectedChannelCallback() {
            @Override
            public <T> T doSelectChannel(ChannelContext<T> channelContext) {
                return channelContext.getChannel(address);
            }
        }, timeout, timeUnit);
    }


    default Object sendAndReturn(Object data, Partitioner partitioner) {
        return sendAndReturn(data, new SelectedChannelCallback() {
            @Override
            public <T> T doSelectChannel(ChannelContext<T> channelContext) {
                return channelContext.selectChannel(data, partitioner);
            }
        });
    }

    default Object sendAndReturn(Object data, Partitioner partitioner, long timeout,
            TimeUnit timeUnit) {
        return sendAndReturn(data, new SelectedChannelCallback() {
            @Override
            public <T> T doSelectChannel(ChannelContext<T> channelContext) {
                return channelContext.selectChannel(data, partitioner);
            }
        }, timeout, timeUnit);
    }

}
