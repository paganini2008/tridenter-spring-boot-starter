package com.github.dingo.netty;

import com.github.dingo.Packet;
import com.github.dingo.TransmitterNioProperties;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: NettyClientKeepAlivePolicy
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class NettyClientKeepAlivePolicy extends KeepAlivePolicy {

    private final TransmitterNioProperties nioProperties;

    protected void whenWriterIdle(ChannelHandlerContext ctx) {
        if (nioProperties.getClient().isKeepAlive()) {
            ctx.channel().writeAndFlush(Packet.PING);
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "Closing the channel because a keep-alive response "
                                + "message was not sent within {} second(s).",
                        nioProperties.getClient().getWriterIdleTimeout());
            }
            ctx.channel().close();
        }
    }

}
