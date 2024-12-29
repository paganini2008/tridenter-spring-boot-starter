package com.github.doodler.common.transmitter.serializer;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.doodler.common.transmitter.Packet;

/**
 * 
 * @Description: JacksonSerializer
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class JacksonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    public JacksonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(Packet packet) {
        try {
            return objectMapper.writeValueAsBytes(packet);
        } catch (IOException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    @Override
    public Packet deserialize(byte[] bytes) {
        Packet packet;
        try {
            packet = objectMapper.readValue(bytes, Packet.class);
            packet.setLength(bytes.length);
        } catch (IOException e) {
            throw new SerializationException(e.getMessage(), e);
        }
        return packet;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

}
