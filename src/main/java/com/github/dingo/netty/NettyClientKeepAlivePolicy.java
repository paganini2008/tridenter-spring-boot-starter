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
