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
package com.github.dingo;

import org.springframework.core.Ordered;
import com.github.doodler.common.events.Context;
import com.github.doodler.common.events.EventSubscriber;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: LoggingPacketSubscriber
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
@Slf4j
public class LoggingPacketSubscriber implements EventSubscriber<Packet> {

    @Override
    public void consume(Packet event, Context context) {
        if (log.isTraceEnabled()) {
            log.trace("Received packet: {}", event.toString());
        }
    }

    @Override
    public void onError(Packet event, Throwable e, Context context) {
        if (log.isErrorEnabled()) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
