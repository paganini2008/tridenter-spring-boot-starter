package com.github.doodler.common.transmitter;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 
 * @Description: PacketFilterExecution
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public final class PacketFilterExecution {

    public PacketFilterExecution(List<PacketFilter> packetFilters) {
        this.packetFilterChain = getPacketFilterChain(packetFilters);
    }

    private final PacketFilterChain packetFilterChain;

    private PacketFilterChain getPacketFilterChain(List<PacketFilter> packetFilters) {
        if (CollectionUtils.isEmpty(packetFilters)) {
            return null;
        }
        PacketFilterChain packetFilterChain = new PacketFilterChain(packetFilters.get(0));
        PacketFilterChain next = packetFilterChain;
        for (int i = 1; i < packetFilters.size(); i++) {
            next.setNextFilter(packetFilters.get(i));
            next = packetFilterChain.next;
        }
        return packetFilterChain;
    }

    public Object executeFilterChain(Packet packet) {
        return executeFilterChain(packetFilterChain, packet);
    }

    Object executeFilterChain(PacketFilterChain filterChain, Packet packet) {
        if (filterChain == null) {
            return packet;
        }
        if (filterChain.shouldFilter(packet)) {
            return filterChain.doFilter(packet);
        }
        return executeFilterChain(filterChain.next, packet);
    }

}
