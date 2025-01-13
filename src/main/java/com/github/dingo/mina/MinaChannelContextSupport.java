package com.github.dingo.mina;

import java.net.SocketAddress;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.dingo.ChannelContext;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ConnectionKeeper;
import com.github.dingo.Packet;
import com.github.dingo.RequestFutureHolder;
import com.github.dingo.ChannelEvent.EventType;

/**
 * 
 * @Description: MinaChannelContextSupport
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public abstract class MinaChannelContextSupport extends IoHandlerAdapter
        implements ChannelContext<IoSession> {

    protected Logger log = LoggerFactory.getLogger(getClass());

    private ConnectionKeeper connectionKeeper;
    private ChannelEventListener<IoSession> channelEventListener;

    public ConnectionKeeper getConnectionKeeper() {
        return connectionKeeper;
    }

    public void setConnectionKeeper(ConnectionKeeper connectionKeeper) {
        this.connectionKeeper = connectionKeeper;
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        addChannel(session);

        fireChannelEvent(session, EventType.CONNECTED, null);

        if (log.isInfoEnabled()) {
            log.info("Current active channel's count: " + countOfChannels());
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        removeChannel(session.getRemoteAddress());

        fireReconnectionIfNecessary(session.getRemoteAddress());
        fireChannelEvent(session, EventType.CLOSED, null);

        if (log.isInfoEnabled()) {
            log.info("Current active channel's count: " + countOfChannels());
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        session.closeNow();

        SocketAddress remoteAddress = session.getRemoteAddress();
        removeChannel(remoteAddress);

        fireReconnectionIfNecessary(session.getRemoteAddress());
        fireChannelEvent(session, EventType.ERROR, cause);
    }



    @Override
    public void messageReceived(IoSession session, Object data) throws Exception {
        if (isPong(data)) {
            fireChannelEvent(session, EventType.PONG, null);
        } else {
            String requestId = (String) session.getAttribute(MinaClient.KEY_REQUEST_ID);
            if (StringUtils.isNotBlank(requestId)) {
                RequestFutureHolder.getRequest(requestId).complete(data);
            }
        }
    }

    private boolean isPong(Object data) {
        return (data instanceof Packet) && ((Packet) data).isPong();
    }

    public void setChannelEventListener(ChannelEventListener<IoSession> channelEventListener) {
        this.channelEventListener = channelEventListener;
    }

    public ChannelEventListener<IoSession> getChannelEventListener() {
        return channelEventListener;
    }

    private void fireChannelEvent(IoSession channel, EventType eventType, Throwable cause) {
        if (channelEventListener != null) {
            channelEventListener
                    .fireChannelEvent(new ChannelEvent<IoSession>(channel, eventType, cause));
        }
    }

    private void fireReconnectionIfNecessary(SocketAddress remoteAddress) {
        if (connectionKeeper != null) {
            connectionKeeper.reconnect(remoteAddress);
        }
    }

}
