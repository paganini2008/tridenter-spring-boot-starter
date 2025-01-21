package com.github.dingo;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @Description: NioClient
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface NioClient extends LifeCycle, NioConnection, Client {

    void watchConnection(int checkInterval, TimeUnit timeUnit, int maxAttempts);

    void keep(SocketAddress socketAddress, HandshakeCallback handshakeCallback);

    void fireReconnection(SocketAddress socketAddress);
}
