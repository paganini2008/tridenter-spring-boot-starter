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
package com.github.dingo.grizzly;

import org.glassfish.grizzly.Connection;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ChannelEvent.EventType;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: GrizzlyChannelEventListener
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
@Slf4j
public class GrizzlyChannelEventListener implements ChannelEventListener<Connection<?>> {

    @Override
    public void fireChannelEvent(ChannelEvent<Connection<?>> channelEvent) {
        if (log.isTraceEnabled()) {
            Connection<?> connection = channelEvent.getSource();
            EventType eventType = channelEvent.getEventType();
            String prefix = channelEvent.isServerSide() ? "ServerSide" : "ClientSide";
            switch (eventType) {
                case CONNECTED:
                    log.trace("{}: {} ->> {} has established connection.", prefix,
                            connection.getLocalAddress(), connection.getPeerAddress());
                    break;
                case CLOSED:
                    log.trace("{}: {} ->> {} has loss connection.", prefix,
                            connection.getLocalAddress(), connection.getPeerAddress());
                    break;
                case PING:
                    log.trace("{}: {} ->> {} send a ping.", prefix, connection.getPeerAddress(),
                            connection.getLocalAddress());
                    break;
                case PONG:
                    log.trace("{}: {} ->> {} send a pong.", prefix, connection.getPeerAddress(),
                            connection.getLocalAddress());
                    break;
                case ERROR:
                    log.trace("{}: {} ->> {} has loss connection for fatal reason: {}", prefix,
                            connection.getLocalAddress(), connection.getPeerAddress(),
                            channelEvent.getCause().getMessage(), channelEvent.getCause());
                    break;
            }
        }
    }

}
