package com.github.doodler.common.transmitter.netty;

import com.github.doodler.common.transmitter.KeepAlivePolicy;
import com.github.doodler.common.transmitter.Packet;
import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @Description: NettyClientKeepAlivePolicy
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public class NettyClientKeepAlivePolicy extends KeepAlivePolicy {

    protected void whenWriterIdle(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(Packet.PING);
    }

}
