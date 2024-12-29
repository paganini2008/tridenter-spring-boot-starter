package com.github.doodler.common.transmitter.serializer;

import java.io.IOException;
import com.github.doodler.common.transmitter.Packet;

/**
 * 
 * @Description: KryoSerializer
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public class KryoSerializer implements Serializer {

    @Override
    public byte[] serialize(Packet packet) throws IOException {
        return KryoUtils.serializeToBytes(packet);
    }

    @Override
    public Packet deserialize(byte[] bytes) throws IOException {
        return KryoUtils.deserializeFromBytes(bytes, Packet.class);
    }



}
