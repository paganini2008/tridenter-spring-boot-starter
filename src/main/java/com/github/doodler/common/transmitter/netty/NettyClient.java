package com.github.doodler.common.transmitter.netty;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.doodler.common.transmitter.ChannelEventListener;
import com.github.doodler.common.transmitter.ConnectionKeeper;
import com.github.doodler.common.transmitter.HandshakeCallback;
import com.github.doodler.common.transmitter.MessageCodecFactory;
import com.github.doodler.common.transmitter.NioClient;
import com.github.doodler.common.transmitter.Packet;
import com.github.doodler.common.transmitter.Partitioner;
import com.github.doodler.common.transmitter.TransmitterNioProperties;
import com.github.doodler.common.transmitter.TransportClientException;
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
import io.netty.util.concurrent.GenericFutureListener;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: NettyClient
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class NettyClient implements NioClient {

    private final NettyChannelContext channelContext = new NettyChannelContext();
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Autowired
    private MessageCodecFactory codecFactory;

    @Override
    public void watchConnection(int checkInterval, TimeUnit timeUnit) {
        this.channelContext
                .setConnectionKeeper(new ConnectionKeeper(checkInterval, timeUnit, this));
    }

    public void setChannelEventListener(ChannelEventListener<Channel> channelEventListener) {
        this.channelContext.setChannelEventListener(channelEventListener);
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
                pipeline.addLast(new NettyClientKeepAlivePolicy());
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
                                ConnectionKeeper connectionKeeper =
                                        channelContext.getConnectionKeeper();
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
            throw new TransportClientException(e.getMessage(), e);
        }

    }

    @Override
    public void send(Object data) {
        channelContext.getChannels().forEach(channel -> {
            doSend(channel, data);
        });
    }

    @Override
    public void send(SocketAddress address, Object data) {
        Channel channel = channelContext.getChannel(address);
        if (channel != null) {
            doSend(channel, data);
        }
    }

    @Override
    public void send(Object data, Partitioner partitioner) {
        Channel channel = channelContext.selectChannel(data, partitioner);
        if (channel != null) {
            doSend(channel, data);
        }
    }

    protected void doSend(Channel channel, Object data) {
        try {
            if (data instanceof CharSequence) {
                channel.writeAndFlush(Packet.byString(((CharSequence) data).toString()));
            } else if (data instanceof Packet) {
                channel.writeAndFlush(data);
            }
        } catch (Exception e) {
            throw new TransportClientException(e.getMessage(), e);
        }
    }

    public void close() {
        try {
            channelContext.getChannels().forEach(channel -> {
                channel.close();
            });
        } catch (Exception e) {
            throw new TransportClientException(e.getMessage(), e);
        }
        try {
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            throw new TransportClientException(e.getMessage(), e);
        }
        opened.set(false);
    }

    public boolean isConnected(SocketAddress remoteAddress) {
        Channel channel = channelContext.getChannel(remoteAddress);
        return channel != null && channel.isActive();
    }

}
