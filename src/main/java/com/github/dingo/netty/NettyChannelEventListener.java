package com.github.dingo.netty;

import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ChannelEvent.EventType;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: NettyChannelEventListener
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
@Slf4j
public class NettyChannelEventListener implements ChannelEventListener<Channel> {

    @Override
    public void fireChannelEvent(ChannelEvent<Channel> channelEvent) {
        if (log.isTraceEnabled()) {
            Channel channel = channelEvent.getSource();
            EventType eventType = channelEvent.getEventType();
            String prefix = channelEvent.isServerSide() ? "ServerSide" : "ClientSide";
            switch (eventType) {
                case CONNECTED:
                    log.trace("{}: {} ->> {} has established connection.", prefix,
                            channel.localAddress(), channel.remoteAddress());
                    break;
                case CLOSED:
                    log.trace("{}: {} ->> {} has lost connection.", prefix, channel.localAddress(),
                            channel.remoteAddress());
                    break;
                case PING:
                    log.trace("{}: {} ->> {} send a ping.", prefix, channel.remoteAddress(),
                            channel.localAddress());
                    break;
                case PONG:
                    log.trace("{}: {} ->> {} send a pong.", prefix, channel.remoteAddress(),
                            channel.localAddress());
                    break;
                case ERROR:
                    log.trace("{}: {} ->> {} has loss connection for fatal reason: {}", prefix,
                            channel.localAddress(), channel.remoteAddress(),
                            channelEvent.getCause().getMessage(), channelEvent.getCause());
                    break;
            }
        }
    }

}
