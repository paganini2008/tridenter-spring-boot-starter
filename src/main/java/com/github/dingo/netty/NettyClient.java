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
package com.github.dingo.netty;

import static com.github.dingo.TransmitterConstants.MODE_ASYNC;
import static com.github.dingo.TransmitterConstants.MODE_SYNC;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ChannelSwitcher;
import com.github.dingo.DataAccessTransmitterClientException;
import com.github.dingo.HandshakeCallback;
import com.github.dingo.MessageCodecFactory;
import com.github.dingo.NioClient;
import com.github.dingo.NioConnectionKeeper;
import com.github.dingo.Packet;
import com.github.dingo.Partitioner;
import com.github.dingo.RequestFutureHolder;
import com.github.dingo.SelectedChannelCallback;
import com.github.dingo.TransmitterClientException;
import com.github.dingo.TransmitterNioProperties;
import com.github.dingo.TransmitterTimeoutException;
import com.github.doodler.common.context.InstanceId;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 
 * @Description: NettyClient
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public class NettyClient implements NioClient {

    static final AttributeKey<String> REQUEST_ID = AttributeKey.valueOf("requestId");

    private final NettyChannelContext channelContext = new NettyChannelContext();
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;

    @Autowired
    private InstanceId instanceId;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Autowired
    private ChannelSwitcher channelSwitcher;

    @Lazy
    @Autowired
    private MessageCodecFactory codecFactory;

    @Autowired
    public void setChannelEventListeners(
            List<ChannelEventListener<Channel>> channelEventListeners) {
        this.channelContext.setChannelEventListeners(channelEventListeners);
    }

    @Override
    public void watchConnection(int checkInterval, TimeUnit timeUnit, int maxAttempts) {
        this.channelContext.setNioConnectionKeeper(
                new NioConnectionKeeper(checkInterval, timeUnit, maxAttempts, this));
    }

    @Override
    public void keep(SocketAddress remoteAddress, HandshakeCallback handshakeCallback) {
        NioConnectionKeeper connectionKeeper = channelContext.getNioConnectionKeeper();
        if (connectionKeeper != null) {
            connectionKeeper.keep(remoteAddress, handshakeCallback);
        }
        if (handshakeCallback != null) {
            handshakeCallback.operationComplete(remoteAddress);
        }
    }

    @Override
    public void fireReconnection(SocketAddress remoteAddress) {
        this.channelContext.getNioConnectionKeeper().reconnect(remoteAddress);
    }

    public void open() {
        TransmitterNioProperties.NioClient clientConfig = nioProperties.getClient();
        final int nThreads = clientConfig.getThreadCount() > 0 ? clientConfig.getThreadCount()
                : Runtime.getRuntime().availableProcessors() * 2;
        workerGroup = new NioEventLoopGroup(nThreads);
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectionTimeout())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSenderBufferSize());
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(clientConfig.getReaderIdleTimeout(),
                        clientConfig.getWriterIdleTimeout(), clientConfig.getAllIdleTimeout(),
                        TimeUnit.SECONDS));
                pipeline.addLast(new NettyClientKeepAlivePolicy(nioProperties));
                pipeline.addLast(codecFactory.getEncoder(), codecFactory.getDecoder());
                pipeline.addLast(channelContext);
            }
        });
        opened.set(true);
    }

    public boolean isOpened() {
        return opened.get();
    }

    public void connect(final SocketAddress remoteAddress,
            final HandshakeCallback handshakeCallback) {
        if (isConnected(remoteAddress)) {
            return;
        }
        try {
            bootstrap.connect(remoteAddress)
                    .addListener(new GenericFutureListener<ChannelFuture>() {
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                NioConnectionKeeper connectionKeeper =
                                        channelContext.getNioConnectionKeeper();
                                if (connectionKeeper != null) {
                                    connectionKeeper.keep(remoteAddress, handshakeCallback);
                                }
                                if (handshakeCallback != null) {
                                    handshakeCallback.operationComplete(remoteAddress);
                                }
                            }
                        }
                    }).sync();
        } catch (InterruptedException e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }

    }

    @Override
    public void send(Object data) {
        if (channelContext.getChannels().isEmpty()) {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
        channelContext.getChannels().forEach(channel -> {
            doSend(null, channel, data, MODE_ASYNC);
        });
    }

    @Override
    public void send(Object data, SocketAddress address) {
        Channel channel = channelContext.getChannel(address);
        if (channel != null) {
            doSend(null, channel, data, MODE_ASYNC);
        } else {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
    }

    @Override
    public void send(Object data, Partitioner partitioner) {
        Channel channel = channelContext.selectChannel(data, partitioner);
        if (channel != null) {
            doSend(null, channel, data, MODE_ASYNC);
        } else {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
    }

    @Override
    public Object sendAndReturn(Object data, SelectedChannelCallback callback) {
        Channel channel = callback.doSelectChannel(channelContext);
        if (channel != null) {
            String requestId = UUID.randomUUID().toString();
            channel.attr(REQUEST_ID).set(requestId);
            CompletableFuture<Object> completableFuture = RequestFutureHolder.getRequest(requestId);
            doSend(requestId, channel, data, MODE_SYNC);
            Packet packet = null;
            try {
                packet = (Packet) completableFuture.get();
                if (packet.containsKey("errorMsg") || packet.containsKey("errorDetails")) {
                    throw new DataAccessTransmitterClientException(
                            packet.getStringField("errorMsg"),
                            packet.getStringField("errorDetails"));
                }
                return packet.getObject();
            } catch (TransmitterClientException e) {
                throw e;
            } catch (Exception e) {
                throw new TransmitterClientException(e.getMessage(), e);
            } finally {
                RequestFutureHolder.removeRequest(requestId);
            }
        }
        throw new DataAccessTransmitterClientException("No available channel found");
    }

    @Override
    public Object sendAndReturn(Object data, SelectedChannelCallback callback, long timeout,
            TimeUnit timeUnit) {
        Channel channel = callback.doSelectChannel(channelContext);
        if (channel != null) {
            String requestId = UUID.randomUUID().toString();
            channel.attr(REQUEST_ID).set(requestId);
            CompletableFuture<Object> completableFuture = RequestFutureHolder.getRequest(requestId);
            doSend(requestId, channel, data, MODE_SYNC);
            Packet packet = null;
            try {
                packet = (Packet) completableFuture.get(timeout, timeUnit);
                if (packet.containsKey("errorMsg") || packet.containsKey("errorDetails")) {
                    throw new DataAccessTransmitterClientException(
                            packet.getStringField("errorMsg"),
                            packet.getStringField("errorDetails"));
                }
                return packet.getObject();
            } catch (TimeoutException e) {
                throw new TransmitterTimeoutException(
                        "Execute timeout: " + timeout + " " + timeUnit.name().toLowerCase());
            } catch (TransmitterClientException e) {
                throw e;
            } catch (Exception e) {
                throw new TransmitterClientException(e.getMessage(), e);
            } finally {
                RequestFutureHolder.removeRequest(requestId);
            }
        }
        throw new DataAccessTransmitterClientException("No available channel found");
    }

    private void doSend(String requestId, Channel channel, Object data, String mode) {
        try {
            Packet packet = null;
            if (data instanceof CharSequence) {
                packet = Packet.wrap(((CharSequence) data).toString());
            } else if (data instanceof Packet) {
                packet = (Packet) data;
            }
            if (packet != null) {
                packet.setMode(mode);
                packet.setField("requestId", requestId);
                packet.setField("instanceId", instanceId.get());
                if (channelSwitcher.canAccess(channel.remoteAddress())) {
                    channel.writeAndFlush(packet);
                } else {
                    getAvailableChannel().writeAndFlush(packet);
                }
            }
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
    }

    private Channel getAvailableChannel() {
        List<Channel> list = channelContext.getChannels(sa -> channelSwitcher.canAccess(sa));
        if (CollectionUtils.isEmpty(list)) {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
        return list.get(0);
    }

    @Override
    public void close() {
        try {
            channelContext.getChannels().forEach(channel -> {
                channel.close();
            });
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
        try {
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
        opened.set(false);
    }

    @Override
    public boolean isConnected(SocketAddress remoteAddress) {
        Channel channel = channelContext.getChannel(remoteAddress);
        return channel != null && channel.isActive();
    }

}
