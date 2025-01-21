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
