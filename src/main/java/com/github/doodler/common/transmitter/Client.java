package com.github.doodler.common.transmitter;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import com.github.doodler.common.utils.NetUtils;

/**
 * 
 * @Description: Client
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface Client {

    void send(Object data);

    void send(SocketAddress socketAddress, Object data);

    void send(Object data, Partitioner partitioner);

    default void send(String serverLocation, Object data) {
        send(NetUtils.parse(serverLocation), data);
    }

    Object sendAndReturn(SocketAddress address, Object data);

    Object sendAndReturn(SocketAddress address, Object data, long timeout, TimeUnit timeUnit);

    Object sendAndReturn(Object data, Partitioner partitioner);

    Object sendAndReturn(Object data, Partitioner partitioner, long timeout, TimeUnit timeUnit);

    default Object sendAndReturn(String serverLocation, Object data) {
        return sendAndReturn(NetUtils.parse(serverLocation), data);
    }

    default Object sendAndReturn(String serverLocation, Object data, long timeout,
            TimeUnit timeUnit) {
        return sendAndReturn(NetUtils.parse(serverLocation), data, timeout, timeUnit);
    }

}
