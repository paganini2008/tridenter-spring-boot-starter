package com.github.dingo.grizzly;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Transformer;

import com.github.dingo.Packet;

/**
 * 
 * @Description: PacketCodecFactory
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
public interface PacketCodecFactory {

    Transformer<Packet, Buffer> getEncoder();

    Transformer<Buffer, Packet> getDecoder();

}
