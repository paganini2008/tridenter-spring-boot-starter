package com.github.dingo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 
 * @Description: NioConnection
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public interface NioConnection {

    default void connect(String serviceLocation, HandshakeCallback handshakeCallback) {
        int index = serviceLocation.indexOf(":");
        if (index == -1) {
            throw new IllegalArgumentException(serviceLocation);
        }
        String hostName = serviceLocation.substring(0, index);
        int port = Integer.parseInt(serviceLocation.substring(index + 1));
        connect(new InetSocketAddress(hostName, port), handshakeCallback);
    }

    void connect(SocketAddress remoteAddress, HandshakeCallback handshakeCallback);

    boolean isConnected(SocketAddress remoteAddress);

}
