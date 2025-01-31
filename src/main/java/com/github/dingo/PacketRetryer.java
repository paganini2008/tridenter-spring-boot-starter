package com.github.dingo;

import static com.github.dingo.PacketKeywords.RETRY_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.github.doodler.common.events.EventPublisher;
import com.github.doodler.common.retry.RetryQueue;
import com.github.doodler.common.utils.SimpleTimer;

/**
 * 
 * @Description: PacketRetryer
 * @Author: Fred Feng
 * @Date: 26/01/2025
 * @Version 1.0.0
 */
public class PacketRetryer extends SimpleTimer {

    public PacketRetryer(TransmitterEventProperties eventProperties, RetryQueue retryQueue,
            EventPublisher<Packet> eventPublisher) {
        super(eventProperties.getFailureRetryInterval(), TimeUnit.SECONDS);
        this.retryQueue = retryQueue;
        this.eventProperties = eventProperties;
        this.eventPublisher = eventPublisher;
    }

    private final TransmitterEventProperties eventProperties;
    private final RetryQueue retryQueue;
    private final EventPublisher<Packet> eventPublisher;

    public void backfill(Packet packet) {
        if (eventProperties.isFailureRetryEnabled()) {
            int retryCount = (Integer) packet.getField(RETRY_COUNT, 0);
            if (retryCount < eventProperties.getFailureRetryTimes()) {
                packet.setField(RETRY_COUNT, retryCount + 1);
                retryQueue.putObject(packet);
            }
        }
    }

    @Override
    public boolean change() throws Exception {
        if (retryQueue.size() == 0) {
            return true;
        }
        List<Object> retries = new ArrayList<Object>();
        retryQueue.drainTo(retries);
        for (Object data : retries) {
            eventPublisher.publish((Packet) data);
        }
        return true;
    }

}
