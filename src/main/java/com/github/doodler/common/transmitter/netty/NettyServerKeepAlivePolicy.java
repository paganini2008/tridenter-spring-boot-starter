package com.github.doodler.common.transmitter.netty;

import org.springframework.beans.factory.annotation.Autowired;
import com.github.doodler.common.transmitter.ChannelEvent;
import com.github.doodler.common.transmitter.ChannelEvent.EventType;
import com.github.doodler.common.transmitter.ChannelEventListener;
import com.github.doodler.common.transmitter.KeepAlivePolicy;
import com.github.doodler.common.transmitter.Packet;
import com.github.doodler.common.transmitter.TransmitterNioProperties;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: NettyServerKeepAlivePolicy
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Slf4j
public class NettyServerKeepAlivePolicy extends KeepAlivePolicy {

    @Autowired
    private TransmitterNioProperties transmitterProperties;

    @Autowired(required = false)
    private ChannelEventListener<Channel> channelEventListener;

    @Override
    protected void whenReaderIdle(ChannelHandlerContext ctx) {
        if (log.isInfoEnabled()) {
            log.info("A keep-alive message was not received within {} second(s).",
                    transmitterProperties.getServer().getReaderIdleTimeout());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
        if (isPing(data)) {
            if (channelEventListener != null) {
                channelEventListener.fireChannelEvent(
                        new ChannelEvent<Channel>(ctx.channel(), EventType.PING, null));
            }
            ctx.writeAndFlush(Packet.PONG);
        } else {
            ctx.fireChannelRead(data);
        }
    }

    private boolean isPing(Object data) {
        return (data instanceof Packet) && ((Packet) data).isPing();
    }

}
