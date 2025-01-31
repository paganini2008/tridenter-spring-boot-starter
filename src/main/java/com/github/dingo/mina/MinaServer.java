package com.github.dingo.mina;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.NioServer;
import com.github.dingo.Packet;
import com.github.dingo.TransmitterNioProperties;
import com.github.dingo.TransmitterServerException;
import com.github.doodler.common.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: MinaServer
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class MinaServer implements NioServer {

    static {
        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
    }

    private final AtomicBoolean started = new AtomicBoolean(false);
    private NioSocketAcceptor ioAcceptor;
    private InetSocketAddress localAddress;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Autowired
    private MinaServerHandler serverHandler;

    @Autowired
    private ProtocolCodecFactory codecFactory;

    @Autowired(required = false)
    private ChannelEventListener<IoSession> channelEventListener;

    @Override
    public SocketAddress start() {
        if (isStarted()) {
            throw new IllegalStateException("MinaServer has been started.");
        }
        TransmitterNioProperties.NioServer serverConfig = nioProperties.getServer();
        final int nThreads = serverConfig.getThreadCount() > 0 ? serverConfig.getThreadCount()
                : Runtime.getRuntime().availableProcessors() * 2;
        ioAcceptor = new NioSocketAcceptor(nThreads);
        ioAcceptor.setBacklog(serverConfig.getBacklog());
        SocketSessionConfig sessionConfig = ioAcceptor.getSessionConfig();
        sessionConfig.setKeepAlive(true);
        sessionConfig.setReuseAddress(true);
        sessionConfig.setReadBufferSize(serverConfig.getReaderBufferSize());
        sessionConfig.setReceiveBufferSize(serverConfig.getReceiverBufferSize());
        sessionConfig.setSoLinger(0);
        ioAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));

        KeepAliveFilter heartBeat = new KeepAliveFilter(new ServerKeepAliveMessageFactory(),
                IdleStatus.READER_IDLE, KeepAliveRequestTimeoutHandler.LOG,
                serverConfig.getReaderIdleTimeout(), serverConfig.getReaderIdleTimeout());
        heartBeat.setForwardEvent(true);
        ioAcceptor.getFilterChain().addLast("heartbeat", heartBeat);

        ioAcceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(nThreads));
        ioAcceptor.setHandler(serverHandler);
        int port = NetUtils.getRandomPort(PORT_RANGE_BEGIN, PORT_RANGE_END);
        try {
            localAddress = StringUtils.isNotBlank(serverConfig.getBindHostName())
                    ? new InetSocketAddress(serverConfig.getBindHostName(), port)
                    : new InetSocketAddress(port);
            ioAcceptor.bind(localAddress);
            started.set(true);
            log.info("MinaServer is started on: " + localAddress);
        } catch (Exception e) {
            throw new TransmitterServerException(e.getMessage(), e);
        }
        return localAddress;
    }

    @Override
    public void stop() {
        if (ioAcceptor == null || !isStarted()) {
            return;
        }
        try {
            ioAcceptor.unbind(localAddress);
            ExecutorFilter executorFilter =
                    (ExecutorFilter) ioAcceptor.getFilterChain().get("threadPool");
            if (executorFilter != null) {
                executorFilter.destroy();
            }
            ioAcceptor.getFilterChain().clear();
            ioAcceptor.dispose();
            ioAcceptor = null;

            started.set(false);
            log.info("Mina is closed successfully.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    /**
     * 
     * @Description: ServerKeepAliveMessageFactory
     * @Author: Fred Feng
     * @Date: 08/01/2025
     * @Version 1.0.0
     */
    private class ServerKeepAliveMessageFactory implements KeepAliveMessageFactory {

        public boolean isRequest(IoSession session, Object message) {
            return (message instanceof Packet) && ((Packet) message).isPing();
        }

        public boolean isResponse(IoSession session, Object message) {
            return false;
        }

        public Object getRequest(IoSession session) {
            return null;
        }

        public Object getResponse(IoSession session, Object request) {
            if (channelEventListener != null) {
                channelEventListener.fireChannelEvent(new ChannelEvent<IoSession>(session,
                        ChannelEvent.EventType.PING, true, null));
            }
            return Packet.PONG;
        }
    }

}
