package com.github.dingo.grizzly;

import static com.github.dingo.TransmitterConstants.MODE_ASYNC;
import static com.github.dingo.TransmitterConstants.MODE_SYNC;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections4.CollectionUtils;
import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import com.github.doodler.common.context.InstanceId;

/**
 * 
 * @Description: GrizzlyClient
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public class GrizzlyClient implements NioClient {

    static final String KEY_REQUEST_ID = "REQUEST_ID";
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final GrizzlyChannelContext channelContext = new GrizzlyChannelContext();
    private TCPNIOTransport transport;
    private DelayedExecutor delayedExecutor;

    @Autowired
    private InstanceId instanceId;

    @Autowired
    private TransmitterNioProperties nioProperties;

    @Autowired
    private ChannelSwitcher channelSwitcher;

    @Lazy
    @Autowired
    private PacketCodecFactory codecFactory;

    @Autowired
    public void setChannelEventListeners(
            List<ChannelEventListener<Connection<?>>> channelEventListeners) {
        this.channelContext.setChannelEventListeners(channelEventListeners);
    }

    @Override
    public void open() {
        TransmitterNioProperties.NioClient clientConfig = nioProperties.getClient();
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add(new TransportFilter());
        delayedExecutor = IdleTimeoutFilter.createDefaultIdleDelayedExecutor(5, TimeUnit.SECONDS);
        delayedExecutor.start();
        IdleTimeoutFilter timeoutFilter = new IdleTimeoutFilter(delayedExecutor,
                clientConfig.getWriterIdleTimeout(), TimeUnit.SECONDS,
                nioProperties.getClient().isKeepAlive() ? IdleTimeoutPolicies.PING
                        : IdleTimeoutPolicies.CLOSE);
        filterChainBuilder.add(timeoutFilter);
        filterChainBuilder.add(new PacketFilter(codecFactory));
        filterChainBuilder.add(channelContext);
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
        ThreadPoolConfig tpConfig = ThreadPoolConfig.defaultConfig();
        int nThreads = clientConfig.getThreadCount() > 0 ? clientConfig.getThreadCount()
                : Runtime.getRuntime().availableProcessors() * 2;
        tpConfig.setPoolName("GrizzlyClientHandler").setQueueLimit(-1).setCorePoolSize(nThreads)
                .setMaxPoolSize(nThreads).setKeepAliveTime(60L, TimeUnit.SECONDS);
        builder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
        builder.setWorkerThreadPoolConfig(tpConfig);
        builder.setKeepAlive(true).setTcpNoDelay(true)
                .setConnectionTimeout(clientConfig.getConnectionTimeout())
                .setWriteBufferSize(clientConfig.getSenderBufferSize());
        transport = builder.build();
        transport.setProcessor(filterChainBuilder.build());
        try {
            transport.start();
        } catch (IOException e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
        opened.set(true);
    }

    @Override
    public void close() {
        try {
            channelContext.getChannels().forEach(connection -> {
                connection.close();
            });
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
        try {
            delayedExecutor.destroy();
            transport.shutdown(60, TimeUnit.SECONDS);
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
    public void connect(SocketAddress remoteAddress, HandshakeCallback handshakeCallback) {
        if (isConnected(remoteAddress)) {
            return;
        }
        try {
            transport.connect(remoteAddress, new DefaultCompletionHandler(handshakeCallback));
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    private class DefaultCompletionHandler implements CompletionHandler<Connection> {

        private final HandshakeCallback handshakeCallback;

        DefaultCompletionHandler(HandshakeCallback handshakeCallback) {
            this.handshakeCallback = handshakeCallback;
        }

        public void cancelled() {
            throw new TransmitterClientException("Connection is cancelled.");
        }

        public void failed(Throwable e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }

        public void updated(Connection connection) {}

        public void completed(Connection connection) {
            if (connection.isOpen()) {
                SocketAddress remoteAddress = (SocketAddress) connection.getPeerAddress();
                NioConnectionKeeper connectionKeeper = channelContext.getNioConnectionKeeper();
                if (connectionKeeper != null) {
                    connectionKeeper.keep(remoteAddress, handshakeCallback);
                }
                if (handshakeCallback != null) {
                    handshakeCallback.operationComplete(remoteAddress);
                }
            }
        }

    }

    @Override
    public boolean isConnected(SocketAddress remoteAddress) {
        Connection<?> connection = channelContext.getChannel(remoteAddress);
        return connection != null && connection.isOpen();
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
    public void send(Object data) {
        if (channelContext.getChannels().isEmpty()) {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
        channelContext.getChannels().forEach(connection -> {
            doSend(null, connection, data, MODE_ASYNC);
        });
    }

    @Override
    public void send(Object data, SocketAddress address) {
        Connection<?> connection = channelContext.getChannel(address);
        if (connection != null) {
            doSend(null, connection, data, MODE_ASYNC);
        } else {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
    }

    @Override
    public void send(Object data, Partitioner partitioner) {
        Connection<?> connection = channelContext.selectChannel(data, partitioner);
        if (connection != null) {
            doSend(null, connection, data, MODE_ASYNC);
        } else {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
    }

    @Override
    public Object sendAndReturn(Object data, SelectedChannelCallback callback) {
        Connection<?> connection = callback.doSelectChannel(channelContext);
        if (connection != null) {
            String requestId = UUID.randomUUID().toString();
            connection.getAttributes().setAttribute(KEY_REQUEST_ID, requestId);
            CompletableFuture<Object> completableFuture = RequestFutureHolder.getRequest(requestId);
            doSend(requestId, connection, data, MODE_SYNC);
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
        Connection<?> connection = callback.doSelectChannel(channelContext);
        if (connection != null) {
            String requestId = UUID.randomUUID().toString();
            connection.getAttributes().setAttribute(KEY_REQUEST_ID, requestId);
            CompletableFuture<Object> completableFuture = RequestFutureHolder.getRequest(requestId);
            doSend(requestId, connection, data, MODE_SYNC);
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

    private void doSend(String requestId, Connection<?> connection, Object data, String mode) {
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
                if (channelSwitcher.canAccess((SocketAddress) connection.getPeerAddress())) {
                    connection.write(packet);
                } else {
                    getAvailableConnection().write(packet);
                }
            }
        } catch (Exception e) {
            throw new TransmitterClientException(e.getMessage(), e);
        }
    }

    private Connection<?> getAvailableConnection() {
        List<Connection<?>> list = channelContext.getChannels(sa -> channelSwitcher.canAccess(sa));
        if (CollectionUtils.isEmpty(list)) {
            throw new DataAccessTransmitterClientException("No available channel found");
        }
        return list.get(0);
    }



}
