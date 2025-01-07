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
