package com.github.dingo;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 
 * @Description: PacketHandlerExecution
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public final class PacketHandlerExecution {

    public PacketHandlerExecution(List<PacketHandler> packetHandlers) {
        this.packetFilterChain = getPacketFilterChain(packetHandlers);
    }

    private final PacketHandlerChain packetFilterChain;

    private PacketHandlerChain getPacketFilterChain(List<PacketHandler> packetHandlers) {
        if (CollectionUtils.isEmpty(packetHandlers)) {
            return null;
        }
        PacketHandlerChain packetFilterChain = new PacketHandlerChain(packetHandlers.get(0));
        PacketHandlerChain next = packetFilterChain;
        for (int i = 1; i < packetHandlers.size(); i++) {
            next.setNextFilter(packetHandlers.get(i));
            next = packetFilterChain.next;
        }
        return packetFilterChain;
    }

    public Object executeHandlerChain(Packet packet) {
        return executeHandlerChain(packetFilterChain, packet);
    }

    Object executeHandlerChain(PacketHandlerChain filterChain, Packet packet) {
        if (filterChain == null) {
            return packet;
        }
        if (filterChain.shouldFilter(packet)) {
            return filterChain.handle(packet);
        }
        return executeHandlerChain(filterChain.next, packet);
    }

}
