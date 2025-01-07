package com.github.dingo.netty;

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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: NettyServerHandler
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Slf4j
@Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Autowired
    private EventPublisher<Packet> eventPublisher;

    @Autowired(required = false)
    private ChannelEventListener<Channel> channelEventListener;

    @Autowired
    private PacketHandlerExecution packetHandlerExecution;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        fireChannelEvent(ctx.channel(), EventType.CONNECTED, null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        fireChannelEvent(ctx.channel(), EventType.CLOSED, null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error(cause.getMessage(), cause);
        fireChannelEvent(ctx.channel(), EventType.ERROR, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
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
                result.setField("errorMsg", e.getMessage());
                result.setField("errorDetails", ExceptionUtils.toString(e));
            }
            result.setField("server", ctx.channel().localAddress().toString());
            result.setField("salt", IdUtils.getShortUuid());
            ctx.writeAndFlush(result);
        } else {
            eventPublisher.publish(packet);
        }
    }

    private void fireChannelEvent(Channel channel, EventType eventType, Throwable cause) {
        if (channelEventListener != null) {
            channelEventListener
                    .fireChannelEvent(new ChannelEvent<Channel>(channel, eventType, cause));
        }
    }

}
