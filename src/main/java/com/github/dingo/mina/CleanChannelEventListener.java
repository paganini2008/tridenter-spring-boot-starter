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
package com.github.dingo.mina;

import java.net.SocketAddress;
import org.apache.mina.core.session.IoSession;
import com.github.dingo.ChannelEvent;
import com.github.dingo.ChannelEventListener;
import com.github.dingo.ChannelSwitcher;
import com.github.dingo.ChannelEvent.EventType;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: CleanChannelEventListener
 * @Author: Fred Feng
 * @Date: 15/01/2025
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class CleanChannelEventListener implements ChannelEventListener<IoSession> {

    private final ChannelSwitcher channelSwitch;

    @Override
    public void fireChannelEvent(ChannelEvent<IoSession> channelEvent) {
        if (!channelEvent.isServerSide() && (channelEvent.getEventType() == EventType.CLOSED
                || channelEvent.getEventType() == EventType.ERROR)) {
            channelSwitch.remove((SocketAddress) channelEvent.getSource().getRemoteAddress());
        }
    }



}
