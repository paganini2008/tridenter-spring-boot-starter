package com.github.dingo.grizzly;

import java.net.SocketAddress;
import org.glassfish.grizzly.Connection;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ChannelSwitcher;
import com.github.dingo.ChannelEvent.EventType;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: CleanChannelEventListener
 * @Author: Fred Feng
 * @Date: 15/01/2025
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class CleanChannelEventListener implements ChannelEventListener<Connection<?>> {

    private final ChannelSwitcher channelSwitch;

    @Override
    public void fireChannelEvent(ChannelEvent<Connection<?>> channelEvent) {
        if (!channelEvent.isServerSide() && (channelEvent.getEventType() == EventType.CLOSED
                || channelEvent.getEventType() == EventType.ERROR)) {
            channelSwitch.remove((SocketAddress) channelEvent.getSource().getPeerAddress());
        }
    }



}
