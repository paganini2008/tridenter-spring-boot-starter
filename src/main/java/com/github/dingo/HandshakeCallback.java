package com.github.dingo;

import java.net.SocketAddress;

/**
 * 
 * @Description: HandshakeCallback
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@FunctionalInterface
public interface HandshakeCallback {

    void operationComplete(SocketAddress address);

}
