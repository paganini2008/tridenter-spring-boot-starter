package com.github.dingo;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: NioTemplate
 * @Author: Fred Feng
 * @Date: 07/01/2025
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class NioTemplate {

    private final NioClient nioClient;
    private final Partitioner partitioner;

    public void convertAndSendAllAsync(Object payload) {
        nioClient.send(payload);
    }

    public void convertAndSendAsync(Object payload) {
        nioClient.send(payload, partitioner);
    }

    public void convertAndSendAsync(Object payload, String serviceLocation) {
        nioClient.send(payload, serviceLocation);
    }

    public Object convertAndSend(Object payload) {
        return nioClient.sendAndReturn(payload, partitioner);
    }

    public Object convertAndSend(Object payload, long timeout, TimeUnit timeUnit) {
        return nioClient.sendAndReturn(payload, partitioner, timeout, timeUnit);
    }

    public Object convertAndSend(Object payload, String serviceLocation) {
        return nioClient.sendAndReturn(payload, serviceLocation);
    }

    public Object convertAndSend(Object payload, String serviceLocation, long timeout,
            TimeUnit timeUnit) {
        return nioClient.sendAndReturn(payload, serviceLocation, timeout, timeUnit);
    }

}
