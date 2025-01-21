package com.github.dingo;

import org.springframework.core.Ordered;
import com.github.doodler.common.events.EventSubscriber;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: PerformanceInspectorPacketSubscriber
 * @Author: Fred Feng
 * @Date: 21/01/2025
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class PerformanceInspectorPacketSubscriber implements EventSubscriber<Packet> {

    private final PerformanceInspector performanceInspector;

    @Override
    public void consume(Packet packet) {
        final String instanceId = packet.getStringField("instanceId");
        final long startTime = packet.getTimestamp();
        performanceInspector.update(instanceId, TransmitterConstants.MODE_ASYNC,
                System.currentTimeMillis(), s -> {
                    s.getSample().totalExecutions.increment();
                    s.getSample().accumulatedExecutionTime
                            .add(System.currentTimeMillis() - startTime);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
