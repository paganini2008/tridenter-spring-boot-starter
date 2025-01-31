package com.github.dingo.serializer;

import java.io.IOException;
import java.util.Map;

import com.github.dingo.Packet;

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

    public static void main(String[] args) throws IOException {
        KryoSerializer kryoSerializer = new KryoSerializer();
        Packet packet = Packet.wrap(Map.of("data", new Object[] {"Hello2"}));
        byte[] bytes = kryoSerializer.serialize(packet);
        System.out.println(bytes);
        System.out.println(kryoSerializer.deserialize(bytes));
    }


}
