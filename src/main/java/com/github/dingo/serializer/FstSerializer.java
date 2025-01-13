package com.github.dingo.serializer;

import org.nustaq.serialization.FSTConfiguration;
import com.github.dingo.Packet;

/**
 * 
 * @Description: FstSerializer
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
public class FstSerializer implements Serializer {

    private final FSTConfiguration configuration = FSTConfiguration.createDefaultConfiguration();

    public byte[] serialize(Packet packet) {
        return configuration.asByteArray(packet);
    }

    public Packet deserialize(byte[] bytes) {
        Packet packet = (Packet) configuration.asObject(bytes);
        packet.setLength(bytes.length);
        return packet;
    }

}
