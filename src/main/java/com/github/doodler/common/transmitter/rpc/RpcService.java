package com.github.doodler.common.transmitter.rpc;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.github.doodler.common.transmitter.NioClient;
import com.github.doodler.common.transmitter.Packet;
import com.github.doodler.common.transmitter.Partitioner;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: RpcService
 * @Author: Fred Feng
 * @Date: 30/12/2024
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class RpcService {

    private final NioClient nioClient;
    private final Partitioner partitioner;

    public Object invokeTargetMethod(String className, String beanName, String methodName,
            Object[] arguments, long timeout, TimeUnit timeUnit, int maxRetries) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        return nioClient.sendAndReturn(packet, partitioner, timeout, timeUnit);
    }

    public Object invokeTargetMethod(String serverLocation, String className, String beanName,
            String methodName, Object[] arguments, long timeout, TimeUnit timeUnit,
            int maxRetries) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        return nioClient.sendAndReturn(serverLocation, packet, timeout, timeUnit);
    }

    public Object invokeTargetMethod(String className, String beanName, String methodName,
            Object[] arguments, int maxRetries) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        return nioClient.sendAndReturn(packet, partitioner);
    }

    public Object invokeTargetMethod(String serverLocation, String className, String beanName,
            String methodName, Object[] arguments, int maxRetries) {
        Map<String, Object> kwargs = Map.of("className", className, "beanName", beanName,
                "methodName", methodName, "arguments", arguments);
        Packet packet = Packet.wrap(kwargs);
        return nioClient.sendAndReturn(serverLocation, packet);
    }

}
