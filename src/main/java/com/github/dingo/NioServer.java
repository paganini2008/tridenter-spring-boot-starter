package com.github.dingo;

import java.net.SocketAddress;

/**
 * 
 * @Description: NioServer
 * @Author: Fred Feng
 * @Date: 26/12/2024
 * @Version 1.0.0
 */
public interface NioServer {

    static final int PORT_RANGE_BEGIN = 40000;

    static final int PORT_RANGE_END = 45000;

    SocketAddress start() throws Exception;

    void stop();

    boolean isStarted();

}
