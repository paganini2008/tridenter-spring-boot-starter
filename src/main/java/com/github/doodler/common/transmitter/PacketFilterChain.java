package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: PacketFilterChain
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
public class PacketFilterChain implements PacketFilter {

    private final PacketFilter packetFilter;

    PacketFilterChain(PacketFilter packetFilter) {
        this.packetFilter = packetFilter;
    }

    PacketFilterChain next;

    void setNextFilter(PacketFilter packetFilter) {
        if (next != null) {
            next.setNextFilter(new PacketFilterChain(packetFilter));
        } else {
            next = new PacketFilterChain(packetFilter);
        }
    }

    public boolean shouldFilter(Packet packet) {
        return packetFilter.shouldFilter(packet);
    }

    public Object doFilter(Packet packet) {
        return packetFilter.doFilter(packet);
    }

}
