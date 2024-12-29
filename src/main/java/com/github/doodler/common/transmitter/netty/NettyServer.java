package com.github.doodler.common.transmitter.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.doodler.common.transmitter.KeepAlivePolicy;
import com.github.doodler.common.transmitter.MessageCodecFactory;
import com.github.doodler.common.transmitter.NioServer;
import com.github.doodler.common.transmitter.TransmitterNioProperties;
import com.github.doodler.common.transmitter.TransportClientException;
import com.github.doodler.common.utils.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: NettyServer
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class NettyServer implements NioServer {

    private final AtomicBoolean started = new AtomicBoolean(false);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Autowired
    private NettyServerHandler serverHandler;

    @Autowired
    private MessageCodecFactory codecFactory;

    @Autowired
    private KeepAlivePolicy keepAlivePolicy;

    @Override
    public SocketAddress start() {
        if (isStarted()) {
            throw new IllegalStateException("NettyServer has been started.");
        }
        TransmitterNioProperties.NioServer serverConfig = nioProperties.getServer();
        final int nThreads = serverConfig.getThreadCount() > 0 ? serverConfig.getThreadCount()
                : Runtime.getRuntime().availableProcessors() * 2;
        bossGroup = new NioEventLoopGroup(nThreads);
        workerGroup = new NioEventLoopGroup(nThreads);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, serverConfig.getBacklog());
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, serverConfig.getReceiverBufferSize());
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(serverConfig.getReaderIdleTimeout(),
                        serverConfig.getWriterIdleTimeout(), serverConfig.getAllIdleTimeout(),
                        TimeUnit.SECONDS));
                pipeline.addLast(codecFactory.getEncoder(), codecFactory.getDecoder());
                pipeline.addLast(keepAlivePolicy);
                pipeline.addLast(serverHandler);
            }
        });
        int port = NetUtils.getRandomPort(PORT_RANGE_BEGIN, PORT_RANGE_END);
        InetSocketAddress socketAddress;
        try {
            socketAddress = StringUtils.isNotBlank(serverConfig.getBindHostName())
                    ? new InetSocketAddress(serverConfig.getBindHostName(), port)
                    : new InetSocketAddress(port);
            bootstrap.bind(socketAddress).sync();
            started.set(true);
            log.info("Netty is started on: " + socketAddress);
        } catch (Exception e) {
            throw new TransportClientException(e.getMessage(), e);
        }
        return socketAddress;
    }

    @Override
    public void stop() {
        if (workerGroup == null || bossGroup == null) {
            return;
        }
        if (!isStarted()) {
            throw new IllegalStateException("NettyServer is not started.");
        }
        try {
            Future<?> workerFuture = workerGroup.shutdownGracefully();
            Future<?> bossFuture = bossGroup.shutdownGracefully();
            bossFuture.await();
            workerFuture.await();
            started.set(false);
            log.info("NettyServer has been stoped successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

}
