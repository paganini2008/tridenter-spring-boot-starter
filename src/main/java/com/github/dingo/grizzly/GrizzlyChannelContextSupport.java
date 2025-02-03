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

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.dingo.ChannelContext;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.NioConnectionKeeper;
import com.github.dingo.Packet;
import com.github.dingo.RequestFutureHolder;
import com.github.dingo.ChannelEvent.EventType;

/**
 * 
 * @Description: GrizzlyChannelContextSupport
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public abstract class GrizzlyChannelContextSupport extends BaseFilter
        implements ChannelContext<Connection<?>> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private NioConnectionKeeper connectionKeeper;
    private List<ChannelEventListener<Connection<?>>> channelEventListeners;

    public NioConnectionKeeper getNioConnectionKeeper() {
        return connectionKeeper;
    }

    public void setNioConnectionKeeper(NioConnectionKeeper connectionKeeper) {
        this.connectionKeeper = connectionKeeper;
    }

    @Override
    public NextAction handleConnect(FilterChainContext ctx) throws IOException {
        addChannel(ctx.getConnection());

        fireChannelEvent(ctx.getConnection(), EventType.CONNECTED, null);
        if (log.isInfoEnabled()) {
            log.info("Current active channel's count: " + countOfChannels());
        }
        return ctx.getInvokeAction();
    }

    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {
        SocketAddress address = (SocketAddress) ctx.getConnection().getPeerAddress();
        removeChannel(address);

        fireReconnectionIfNecessary(address);
        fireChannelEvent(ctx.getConnection(), EventType.CLOSED, null);
        if (log.isInfoEnabled()) {
            log.info("Current active channel's count: " + countOfChannels());
        }
        return ctx.getInvokeAction();
    }

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        Packet input = ctx.getMessage();
        if (isPong(input)) {
            fireChannelEvent(ctx.getConnection(), EventType.PONG, null);
            return ctx.getStopAction();
        } else {
            String requestId = (String) ctx.getConnection().getAttributes()
                    .getAttribute(GrizzlyClient.KEY_REQUEST_ID);
            if (StringUtils.isNotBlank(requestId)) {
                RequestFutureHolder.getRequest(requestId).complete(input);
            }
        }
        return ctx.getInvokeAction();
    }

    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
        ctx.getConnection().close();

        SocketAddress address = (SocketAddress) ctx.getConnection().getPeerAddress();
        removeChannel(address);

        fireReconnectionIfNecessary(address);
        fireChannelEvent(ctx.getConnection(), EventType.ERROR, error);
    }

    public List<ChannelEventListener<Connection<?>>> getChannelEventListeners() {
        return channelEventListeners;
    }

    @Override
    public void setChannelEventListeners(
            List<ChannelEventListener<Connection<?>>> channelEventListeners) {
        this.channelEventListeners = channelEventListeners;
    }

    private void fireChannelEvent(Connection<?> channel, EventType eventType, Throwable cause) {
        if (channelEventListeners != null) {
            channelEventListeners.forEach(l -> l.fireChannelEvent(
                    new ChannelEvent<Connection<?>>(channel, eventType, false, cause)));
        }
    }

    private void fireReconnectionIfNecessary(SocketAddress remoteAddress) {
        if (connectionKeeper != null) {
            connectionKeeper.reconnect(remoteAddress);
        }
    }

    private boolean isPong(Object data) {
        return (data instanceof Packet) && ((Packet) data).isPong();
    }

}
