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
package com.github.dingo.mina;

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
import com.github.dingo.ChannelSwitcher;
import com.github.dingo.DataAccessTransmitterClientException;
import com.github.dingo.HandshakeCallback;
import com.github.dingo.NioClient;
import com.github.dingo.NioConnectionKeeper;
import com.github.dingo.Packet;
import com.github.dingo.Partitioner;
import com.github.dingo.RequestFutureHolder;
import com.github.dingo.SelectedChannelCallback;
import com.github.dingo.TransmitterClientException;
import com.github.dingo.TransmitterNioProperties;
import com.github.dingo.TransmitterTimeoutException;
import com.github.dingo.ChannelEvent.EventType;
import com.github.doodler.common.context.InstanceId;

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
    private InstanceId instanceId;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Autowired
    private ChannelSwitcher channelSwitcher;

    @Lazy
    @Autowired
    private ProtocolCodecFactory protocolCodecFactory;

    @Autowired
    public void setChannelEventListeners(
            List<ChannelEventListener<IoSession>> channelEventListeners) {
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

        KeepAliveFilter heartBeat =
                new KeepAliveFilter(new ClientKeepAliveMessageFactory(), IdleStatus.WRITER_IDLE,
                        nioProperties.getClient().isKeepAlive() ? KeepAliveRequestTimeoutHandler.LOG
                                : KeepAliveRequestTimeoutHandler.CLOSE,
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
        if (channelContext.getChannels().isEmpty()) {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
        channelContext.getChannels().forEach(ioSession -> {
            doSend(null, ioSession, data, MODE_ASYNC);
        });
    }

    @Override
    public void send(Object data, SocketAddress address) {
        IoSession ioSession = channelContext.getChannel(address);
        if (ioSession != null) {
            doSend(null, ioSession, data, MODE_ASYNC);
        } else {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
    }

    @Override
    public void send(Object data, Partitioner partitioner) {
        IoSession ioSession = channelContext.selectChannel(data, partitioner);
        if (ioSession != null) {
            doSend(null, ioSession, data, MODE_ASYNC);
        } else {
            throw new DataAccessTransmitterClientException("No available channel found");
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
                packet.setField("instanceId", instanceId.get());
                if (channelSwitcher.canAccess(ioSession.getRemoteAddress())) {
                    ioSession.write(packet);
                } else {
                    getAvailableIoSession().write(packet);
                }

            }
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
    }

    private IoSession getAvailableIoSession() {
        List<IoSession> list = channelContext.getChannels(sa -> channelSwitcher.canAccess(sa));
        if (CollectionUtils.isEmpty(list)) {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
        return list.get(0);
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
            return nioProperties.getClient().isKeepAlive() ? Packet.PING : null;
        }

        public Object getResponse(IoSession session, Object request) {
            List<ChannelEventListener<IoSession>> channelEventListeners =
                    channelContext.getChannelEventListeners();
            if (channelEventListeners != null) {
                channelEventListeners.forEach(l -> l.fireChannelEvent(
                        new ChannelEvent<IoSession>(session, EventType.PONG, false, null)));
            }
            return null;
        }
    }

}
