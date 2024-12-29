/**
 * Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.doodler.common.transmitter.netty;

import java.net.SocketAddress;
import com.github.doodler.common.transmitter.ChannelContext;
import com.github.doodler.common.transmitter.ChannelEvent;
import com.github.doodler.common.transmitter.ChannelEvent.EventType;
import com.github.doodler.common.transmitter.ChannelEventListener;
import com.github.doodler.common.transmitter.ConnectionKeeper;
import com.github.doodler.common.transmitter.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * @Description: NettyChannelContextAware
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public abstract class NettyChannelContextAware extends ChannelInboundHandlerAdapter
        implements ChannelContext<Channel> {

    private ConnectionKeeper connectionKeeper;
    private ChannelEventListener<Channel> channelEventListener;

    public ConnectionKeeper getConnectionKeeper() {
        return connectionKeeper;
    }

    public void setConnectionKeeper(ConnectionKeeper connectionKeeper) {
        this.connectionKeeper = connectionKeeper;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        addChannel(ctx.channel());

        fireChannelEvent(ctx.channel(), EventType.CONNECTED, null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        removeChannel(remoteAddress);

        fireReconnectionIfNecessary(remoteAddress);
        fireChannelEvent(ctx.channel(), EventType.CLOSED, null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();

        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        removeChannel(remoteAddress);

        fireReconnectionIfNecessary(remoteAddress);
        fireChannelEvent(ctx.channel(), EventType.FAULTY, cause);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
        if (isPong(data)) {
            fireChannelEvent(ctx.channel(), EventType.PONG, null);
        }
    }

    private boolean isPong(Object data) {
        return (data instanceof Packet) && ((Packet) data).isPong();
    }

    @Override
    public void setChannelEventListener(ChannelEventListener<Channel> channelEventListener) {
        this.channelEventListener = channelEventListener;
    }

    public ChannelEventListener<Channel> getChannelEventListener() {
        return channelEventListener;
    }

    private void fireChannelEvent(Channel channel, EventType eventType, Throwable cause) {
        if (channelEventListener != null) {
            channelEventListener
                    .fireChannelEvent(new ChannelEvent<Channel>(channel, eventType, cause));
        }
    }

    private void fireReconnectionIfNecessary(SocketAddress remoteAddress) {
        if (connectionKeeper != null) {
            connectionKeeper.reconnect(remoteAddress);
        }
    }

}
