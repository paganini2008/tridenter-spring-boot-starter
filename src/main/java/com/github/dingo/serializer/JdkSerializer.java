package com.github.dingo.serializer;

import org.apache.commons.lang3.SerializationUtils;

import com.github.dingo.Packet;

/**
 * 
 * @Description: JdkSerializer
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class JdkSerializer implements Serializer {

    public byte[] serialize(Packet packet) {
        return SerializationUtils.serialize(packet);
    }

    public Packet deserialize(byte[] bytes) {
        Packet packet = (Packet) SerializationUtils.deserialize(bytes);
        packet.setLength(bytes.length);
        return packet;
    }

}
