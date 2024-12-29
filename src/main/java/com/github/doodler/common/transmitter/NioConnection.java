package com.github.doodler.common.transmitter;

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

    default void connect(String serverLocation, HandshakeCallback handshakeCallback) {
        int index = serverLocation.indexOf(":");
        if (index == -1) {
            throw new IllegalArgumentException(serverLocation);
        }
        String hostName = serverLocation.substring(0, index);
        int port = Integer.parseInt(serverLocation.substring(index + 1));
        connect(InetSocketAddress.createUnresolved(hostName, port), handshakeCallback);
    }

    void connect(SocketAddress remoteAddress, HandshakeCallback handshakeCallback);

    boolean isConnected(SocketAddress remoteAddress);

}
