package com.github.dingo;

/**
 * 
 * @Description: PacketHandler
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public interface PacketHandler {

    default boolean shouldFilter(Packet packet) {
        return getName().equals(packet.getStringField(TransmitterConstants.ATTR_PACKET_HANDLER));
    }

    Object handle(Packet packet);

    default String getName() {
        return getClass().getName();
    }

}
