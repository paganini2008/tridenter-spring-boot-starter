package com.github.dingo.mina;

import java.net.SocketAddress;
import org.apache.mina.core.session.IoSession;
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
public class CleanChannelEventListener implements ChannelEventListener<IoSession> {

    private final ChannelSwitcher channelSwitch;

    @Override
    public void fireChannelEvent(ChannelEvent<IoSession> channelEvent) {
        if (!channelEvent.isServerSide() && (channelEvent.getEventType() == EventType.CLOSED
                || channelEvent.getEventType() == EventType.ERROR)) {
            channelSwitch.remove((SocketAddress) channelEvent.getSource().getRemoteAddress());
        }
    }



}
