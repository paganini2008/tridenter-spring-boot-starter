/*
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dingo.netty;

import static com.github.dingo.TransmitterConstants.MODE_SYNC;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.Packet;
import com.github.dingo.PacketHandlerExecution;
import com.github.dingo.PerformanceInspectorService;
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

    @Autowired
    private PerformanceInspectorService performanceInspector;

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
        if (MODE_SYNC.equalsIgnoreCase(((Packet) message).getMode())) {
            Packet result = ((Packet) message).copy();
            long timestamp = result.getTimestamp();
            String instanceId = result.getStringField("instanceId");
            try {
                Object returnData = packetHandlerExecution.executeHandlerChain(result);
                if (returnData != null) {
                    if (returnData instanceof Packet) {
                        result = (Packet) returnData;
                    } else {
                        result.setObject(returnData);
                    }
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
                result.setField("errorMsg", e.getMessage());
                result.setField("errorDetails", ExceptionUtils.toString(e));
            } finally {
                final Packet tmp = result;
                performanceInspector.update(instanceId, MODE_SYNC, timestamp, s -> {
                    s.getSample().accumulatedExecutionTime
                            .add(System.currentTimeMillis() - timestamp);
                    s.getSample().totalExecutions.increment();
                    s.getSample().timestamp = System.currentTimeMillis();
                    if (tmp.hasField("errorMsg") || tmp.hasField("errorDetails")) {
                        s.getSample().failedExecutions.increment();
                    }
                });
            }
            result.setField("server", ctx.channel().localAddress().toString());
            result.setField("salt", IdUtils.getShortUuid());
            ctx.writeAndFlush(result);
        } else {
            eventPublisher.publish((Packet) message);
        }
    }

    private void fireChannelEvent(Channel channel, EventType eventType, Throwable cause) {
        if (channelEventListener != null) {
            channelEventListener
                    .fireChannelEvent(new ChannelEvent<Channel>(channel, eventType, true, cause));
        }
    }

}
