package com.github.doodler.common.transmitter;

import java.util.EventListener;

/**
 * 
 * @Description: ChannelEventListener
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface ChannelEventListener<T> extends EventListener {

    default void fireChannelEvent(ChannelEvent<T> channelEvent) {}

}
