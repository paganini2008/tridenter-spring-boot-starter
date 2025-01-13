package com.github.dingo.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.Packet;
import com.github.dingo.PacketHandlerExecution;
import com.github.dingo.TransmitterConstants;
import com.github.dingo.ChannelEvent.EventType;
import com.github.doodler.common.events.EventPublisher;
import com.github.doodler.common.utils.ExceptionUtils;
import com.github.doodler.common.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: MinaServerHandler
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class MinaServerHandler extends IoHandlerAdapter {

    @Autowired
    private EventPublisher<Packet> eventPublisher;

    @Autowired(required = false)
    private ChannelEventListener<IoSession> channelEventListener;

    @Autowired
    private PacketHandlerExecution packetHandlerExecution;

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        fireChannelEvent(session, EventType.CONNECTED, null);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        fireChannelEvent(session, EventType.CLOSED, null);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
        fireChannelEvent(session, EventType.ERROR, cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        Packet packet = (Packet) message;
        if (TransmitterConstants.MODE_SYNC.equalsIgnoreCase(packet.getMode())) {
            Packet result = packet.copy();
            try {
                Object returnData = packetHandlerExecution.executeHandlerChain(packet);
                if (returnData != null) {
                    if (returnData instanceof Packet) {
                        result = (Packet) returnData;
                    } else {
                        result.setObject(returnData);
                    }
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                result.setField("errorMsg", e.getMessage());
                result.setField("errorDetails", ExceptionUtils.toString(e));
            }
            result.setField("server", session.getLocalAddress().toString());
            result.setField("salt", IdUtils.getShortUuid());
            session.write(result);
        } else {
            eventPublisher.publish(packet);
        }
    }

    private void fireChannelEvent(IoSession channel, EventType eventType, Throwable cause) {
        if (channelEventListener != null) {
            channelEventListener
                    .fireChannelEvent(new ChannelEvent<IoSession>(channel, eventType, cause));
        }
    }

}
