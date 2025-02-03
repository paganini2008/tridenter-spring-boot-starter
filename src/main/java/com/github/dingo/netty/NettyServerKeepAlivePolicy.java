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

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.Packet;
import com.github.dingo.TransmitterNioProperties;
import com.github.dingo.ChannelEvent.EventType;
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
    private TransmitterNioProperties transmitterNioProperties;

    @Autowired(required = false)
    private List<ChannelEventListener<Channel>> channelEventListeners;

    @Override
    protected void whenReaderIdle(ChannelHandlerContext ctx) {
        if (log.isInfoEnabled()) {
            log.info("A keep-alive message was not received within {} second(s).",
                    transmitterNioProperties.getServer().getReaderIdleTimeout());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
        if (isPing(data)) {
            if (channelEventListeners != null) {
                channelEventListeners.forEach(l -> l.fireChannelEvent(
                        new ChannelEvent<Channel>(ctx.channel(), EventType.PING, true, null)));
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
