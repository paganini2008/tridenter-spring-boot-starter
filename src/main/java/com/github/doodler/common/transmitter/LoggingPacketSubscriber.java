package com.github.doodler.common.transmitter;

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
    public void consume(Packet event) {
        if (log.isTraceEnabled()) {
            log.trace("Received packet: {}", event.toString());
        }
    }

}
