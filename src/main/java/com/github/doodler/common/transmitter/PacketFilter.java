package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: PacketFilter
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public interface PacketFilter {

    default boolean shouldFilter(Packet packet) {
        return getName().equals(packet.getStringField("packetFilter"));
    }

    Object doFilter(Packet packet);

    default String getName() {
        return getClass().getName();
    }

}
