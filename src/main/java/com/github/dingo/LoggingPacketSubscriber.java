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
    public void onError(Packet event, Exception e, Context context) {
        if (log.isErrorEnabled()) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
