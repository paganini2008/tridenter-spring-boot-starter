package com.github.doodler.common.transmitter.netty;

import io.netty.channel.ChannelHandler.Sharable;
import com.github.doodler.common.transmitter.KeepAliveTimeoutException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * @Description: KeepAlivePolicy
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Sharable
public abstract class KeepAlivePolicy extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    whenReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    whenWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    whenBothIdle(ctx);
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    protected void whenReaderIdle(ChannelHandlerContext ctx) {
        throw new KeepAliveTimeoutException("Reading Idle.");
    }

    protected void whenWriterIdle(ChannelHandlerContext ctx) {
        throw new KeepAliveTimeoutException("Writing Idle.");
    }

    protected void whenBothIdle(ChannelHandlerContext ctx) {
        throw new KeepAliveTimeoutException("Reading or Writing Idle.");
    }

}
