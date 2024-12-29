package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: Handler
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface Handler {

    void process(Packet packet);

    default String getTopic() {
        return Packet.DEFAULT_TOPIC;
    }

}
