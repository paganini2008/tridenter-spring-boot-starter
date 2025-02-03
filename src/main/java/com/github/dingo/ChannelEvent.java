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

import java.util.EventObject;

/**
 * 
 * @Description: ChannelEvent
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class ChannelEvent<T> extends EventObject {

    private static final long serialVersionUID = 6921528186565405569L;

    public ChannelEvent(T source, EventType eventType, boolean serverSide) {
        this(source, eventType, serverSide, null);
    }

    public ChannelEvent(T source, EventType eventType, boolean serverSide, Throwable cause) {
        super(source);
        this.eventType = eventType;
        this.cause = cause;
        this.serverSide = serverSide;
    }

    private final EventType eventType;
    private final Throwable cause;
    private final boolean serverSide;

    public EventType getEventType() {
        return eventType;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    public Throwable getCause() {
        return cause;
    }

    @SuppressWarnings("unchecked")
    public T getSource() {
        return (T) super.getSource();
    }

    public static enum EventType {

        CONNECTED, CLOSED, PING, PONG, ERROR;

    }

}
