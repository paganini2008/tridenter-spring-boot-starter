package com.github.doodler.common.transmitter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

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

    default Object sendAndReturn(String serverLocation, Object data) {
        int index = serverLocation.indexOf(":");
        if (index == -1) {
            throw new IllegalArgumentException(serverLocation);
        }
        String hostName = serverLocation.substring(0, index);
        int port = Integer.parseInt(serverLocation.substring(index + 1));
        return sendAndReturn(new InetSocketAddress(hostName, port), data);
    }

    Object sendAndReturn(SocketAddress address, Object data);

    Object sendAndReturn(Object data, Partitioner partitioner);

}
