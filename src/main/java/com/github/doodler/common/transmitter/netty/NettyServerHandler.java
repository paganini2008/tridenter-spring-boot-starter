package com.github.doodler.common.transmitter.netty;

import org.springframework.beans.factory.annotation.Autowired;
import com.github.doodler.common.events.EventPublisher;
import com.github.doodler.common.transmitter.ChannelEvent;
import com.github.doodler.common.transmitter.ChannelEvent.EventType;
import com.github.doodler.common.transmitter.ChannelEventListener;
import com.github.doodler.common.transmitter.Packet;
import com.github.doodler.common.transmitter.PacketReader;
import com.github.doodler.common.transmitter.TransmitterConstants;
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

    @Autowired(required = false)
    private PacketReader packetReader;

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
            if (packetReader != null) {
                Object returnData = packetReader.response(packet);
                if (returnData != null) {
                    if (returnData instanceof Packet) {
                        result = (Packet) returnData;
                    } else {
                        result.setObject(returnData);
                    }
                }
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
