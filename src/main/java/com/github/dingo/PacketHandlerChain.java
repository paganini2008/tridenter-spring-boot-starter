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

/**
 * 
 * @Description: PacketHandlerChain
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
public class PacketHandlerChain implements PacketHandler {

    private final PacketHandler packetHandler;

    PacketHandlerChain(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    PacketHandlerChain next;

    void setNextFilter(PacketHandler packetHandler) {
        if (next != null) {
            next.setNextFilter(new PacketHandlerChain(packetHandler));
        } else {
            next = new PacketHandlerChain(packetHandler);
        }
    }

    public boolean shouldFilter(Packet packet) {
        return packetHandler.shouldFilter(packet);
    }

    public Object handle(Packet packet) {
        return packetHandler.handle(packet);
    }

}
