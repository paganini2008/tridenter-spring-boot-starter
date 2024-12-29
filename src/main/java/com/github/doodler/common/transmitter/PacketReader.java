package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: PacketReader
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
public interface PacketReader {

    Object response(Packet packet);

}
