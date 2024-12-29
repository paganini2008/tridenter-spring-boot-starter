package com.github.doodler.common.transmitter;

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

}
