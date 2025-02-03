/*
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
