package com.github.dingo;

import static com.github.dingo.PacketKeywords.INSTANCE_ID;
import static com.github.dingo.TransmitterConstants.MODE_ASYNC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.github.doodler.common.events.Context;
import com.github.doodler.common.utils.MapUtils;

/**
 * 
 * @Description: NioContext
 * @Author: Fred Feng
 * @Date: 28/01/2025
 * @Version 1.0.0
 */
public final class NioContext extends Context {

    NioContext(PerformanceInspectorService performanceInspectorService) {
        this.performanceInspectorService = performanceInspectorService;
    }

    private final PerformanceInspectorService performanceInspectorService;
    private final Map<String, AtomicInteger> concurrents = new ConcurrentHashMap<>();

    @Override
    protected void beforeConsuming(Object event) {
        Packet packet = (Packet) event;
        String instanceId = packet.getStringField(INSTANCE_ID);
        MapUtils.getOrCreate(concurrents, instanceId, AtomicInteger::new).incrementAndGet();
        long startTime = packet.getTimestamp();
        performanceInspectorService.update(instanceId, MODE_ASYNC, System.currentTimeMillis(),
                s -> {
                    s.getSample().totalExecutions.increment();
                    s.getSample().timestamp = startTime;
                });
    }

    @Override
    protected void completeConsuming(Object event, Exception error) {
        Packet packet = (Packet) event;
        String instanceId = packet.getStringField(INSTANCE_ID);
        long startTime = packet.getTimestamp();
        performanceInspectorService.update(instanceId, MODE_ASYNC, System.currentTimeMillis(),
                s -> {
                    if (error != null) {
                        s.getSample().failedExecutions.increment();
                    }
                    s.getSample().accumulatedExecutionTime
                            .add(System.currentTimeMillis() - startTime);
                });
        concurrents.get(instanceId).decrementAndGet();
    }

    public int getConcurrents(String instanceId) {
        return concurrents.get(instanceId).get();
    }

    public int getConcurrents() {
        if (concurrents.isEmpty()) {
            return 0;
        }
        return concurrents.values().stream().mapToInt(AtomicInteger::get).sum();
    }

}
