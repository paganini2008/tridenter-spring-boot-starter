package com.github.dingo.grizzly;

import org.glassfish.grizzly.Connection;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ChannelEvent.EventType;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: GrizzlyChannelEventListener
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class GrizzlyChannelEventListener implements ChannelEventListener<Connection<?>> {

    @Override
    public void fireChannelEvent(ChannelEvent<Connection<?>> channelEvent) {
        if (log.isTraceEnabled()) {
            Connection<?> connection = channelEvent.getSource();
            EventType eventType = channelEvent.getEventType();
            switch (eventType) {
                case CONNECTED:
                    log.trace("{} ->> {} has established connection.", connection.getLocalAddress(),
                            connection.getPeerAddress());
                    break;
                case CLOSED:
                    log.trace("{} ->> {} has loss connection.", connection.getLocalAddress(),
                            connection.getPeerAddress());
                    break;
                case PING:
                    log.trace(connection.getPeerAddress() + "{} ->> {} send a ping.",
                            connection.getPeerAddress(), connection.getLocalAddress());
                    break;
                case PONG:
                    log.trace("{} ->> {} send a pong.", connection.getPeerAddress(),
                            connection.getLocalAddress());
                    break;
                case ERROR:
                    log.trace("{} ->> {} has loss connection for fatal reason.",
                            connection.getLocalAddress(), connection.getPeerAddress(),
                            channelEvent.getCause());
                    break;
            }
        }
    }

}
