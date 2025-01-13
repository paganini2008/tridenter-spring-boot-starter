package com.github.dingo.mina;

import static com.github.dingo.TransmitterConstants.MODE_ASYNC;
import static com.github.dingo.TransmitterConstants.MODE_SYNC;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ConnectionKeeper;
import com.github.dingo.DataAccessTransmitterClientException;
import com.github.dingo.HandshakeCallback;
import com.github.dingo.NioClient;
import com.github.dingo.Packet;
import com.github.dingo.Partitioner;
import com.github.dingo.RequestFutureHolder;
import com.github.dingo.SelectedChannelCallback;
import com.github.dingo.TransmitterClientException;
import com.github.dingo.TransmitterNioProperties;
import com.github.dingo.ChannelEvent.EventType;

/**
 * 
 * @Description: MinaClient
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public class MinaClient implements NioClient {

    static {
        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
    }

    static final String KEY_REQUEST_ID = "REQUEST_ID";

    private final MinaChannelContext channelContext = new MinaChannelContext();
    private final AtomicBoolean opened = new AtomicBoolean(false);

    private NioSocketConnector connector;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Lazy
    @Autowired
    private ProtocolCodecFactory protocolCodecFactory;

    @Autowired
    public void setChannelEventListener(ChannelEventListener<IoSession> channelEventListener) {
        this.channelContext.setChannelEventListener(channelEventListener);
    }

    @Override
    public void watchConnection(int checkInterval, TimeUnit timeUnit) {
        this.channelContext
                .setConnectionKeeper(new ConnectionKeeper(checkInterval, timeUnit, this));
    }

    @Override
    public void open() {
        TransmitterNioProperties.NioClient clientConfig = nioProperties.getClient();
        int nThreads = clientConfig.getThreadCount() > 0 ? clientConfig.getThreadCount()
                : Runtime.getRuntime().availableProcessors() * 2;
        connector = new NioSocketConnector(nThreads);
        connector.setConnectTimeoutMillis(clientConfig.getConnectionTimeout());
        SocketSessionConfig sessionConfig = connector.getSessionConfig();
        sessionConfig.setKeepAlive(true);
        sessionConfig.setTcpNoDelay(true);
        sessionConfig.setSendBufferSize(clientConfig.getSenderBufferSize());

        KeepAliveFilter heartBeat = new KeepAliveFilter(new ClientKeepAliveMessageFactory(),
                IdleStatus.WRITER_IDLE, KeepAliveRequestTimeoutHandler.LOG,
                clientConfig.getWriterIdleTimeout(), clientConfig.getWriterIdleTimeout());
        heartBeat.setForwardEvent(true);
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(protocolCodecFactory));
        connector.getFilterChain().addLast("heartbeat", heartBeat);
        connector.setHandler(channelContext);

        opened.set(true);
    }

    @Override
    public void close() {
        try {
            channelContext.getChannels().forEach(ioSession -> {
                ioSession.closeNow();
            });
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
        try {
            if (connector != null) {
                connector.getFilterChain().clear();
                connector.dispose();
                connector = null;
            }
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }

        opened.set(false);
    }

    @Override
    public boolean isOpened() {
        return opened.get();
    }

    @Override
    public void connect(final SocketAddress remoteAddress,
            final HandshakeCallback handshakeCallback) {
        if (isConnected(remoteAddress)) {
            return;
        }
        try {
            connector.connect(remoteAddress).addListener(new IoFutureListener<IoFuture>() {
                public void operationComplete(IoFuture future) {
                    if (future.isDone()) {
                        ConnectionKeeper connectionKeeper = channelContext.getConnectionKeeper();
                        if (connectionKeeper != null) {
                            connectionKeeper.keep(remoteAddress, handshakeCallback);
                        }
                        if (handshakeCallback != null) {
                            handshakeCallback.operationComplete(remoteAddress);
                        }
                    }
                }
            }).awaitUninterruptibly();
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected(SocketAddress remoteAddress) {
        IoSession ioSession = channelContext.getChannel(remoteAddress);
        return ioSession != null && ioSession.isConnected();
    }

    @Override
    public void send(Object data) {
        channelContext.getChannels().forEach(ioSession -> {
            doSend(null, ioSession, data, MODE_ASYNC);
        });
    }

    @Override
    public void send(Object data, SocketAddress address) {
        IoSession ioSession = channelContext.getChannel(address);
        if (ioSession != null) {
            doSend(null, ioSession, data, MODE_ASYNC);
        }
    }

    @Override
    public void send(Object data, Partitioner partitioner) {
        IoSession ioSession = channelContext.selectChannel(data, partitioner);
        if (ioSession != null) {
            doSend(null, ioSession, data, MODE_ASYNC);
        }
    }

    @Override
    public Object sendAndReturn(Object data, SelectedChannelCallback callback) {
        IoSession ioSession = callback.doSelectChannel(channelContext);
        if (ioSession != null) {
            String requestId = UUID.randomUUID().toString();
            ioSession.setAttribute(KEY_REQUEST_ID, requestId);
            CompletableFuture<Object> completableFuture = RequestFutureHolder.getRequest(requestId);
            doSend(requestId, ioSession, data, MODE_SYNC);
            Packet packet = null;
            try {
                packet = (Packet) completableFuture.get();
                if (packet.containsKey("errorMsg") || packet.containsKey("errorDetails")) {
                    throw new DataAccessTransmitterClientException(
                            packet.getStringField("errorMsg"),
                            packet.getStringField("errorDetails"));
                }
                return packet.getObject();
            } catch (Exception e) {
                throw new TransmitterClientException(e.getMessage(), e);
            } finally {
                RequestFutureHolder.removeRequest(requestId);
            }
        }
        return null;
    }

    @Override
    public Object sendAndReturn(Object data, SelectedChannelCallback callback, long timeout,
            TimeUnit timeUnit) {
        IoSession ioSession = callback.doSelectChannel(channelContext);
        if (ioSession != null) {
            String requestId = UUID.randomUUID().toString();
            ioSession.setAttribute(KEY_REQUEST_ID, requestId);
            CompletableFuture<Object> completableFuture = RequestFutureHolder.getRequest(requestId);
            doSend(requestId, ioSession, data, MODE_SYNC);
            Packet packet = null;
            try {
                packet = (Packet) completableFuture.get(timeout, timeUnit);
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
        return null;
    }

    private void doSend(String requestId, IoSession ioSession, Object data, String mode) {
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
                ioSession.write(packet);
            }
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
    }

    /**
     * 
     * @Description: ClientKeepAliveMessageFactory
     * @Author: Fred Feng
     * @Date: 09/01/2025
     * @Version 1.0.0
     */
    private class ClientKeepAliveMessageFactory implements KeepAliveMessageFactory {

        public boolean isRequest(IoSession session, Object message) {
            return (message instanceof Packet) && ((Packet) message).isPong();
        }

        public boolean isResponse(IoSession session, Object message) {
            return false;
        }

        public Object getRequest(IoSession session) {
            return Packet.PING;
        }

        public Object getResponse(IoSession session, Object request) {
            ChannelEventListener<IoSession> channelEventListener =
                    channelContext.getChannelEventListener();
            if (channelEventListener != null) {
                channelEventListener.fireChannelEvent(
                        new ChannelEvent<IoSession>(session, EventType.PONG, null));
            }
            return null;
        }
    }

}
