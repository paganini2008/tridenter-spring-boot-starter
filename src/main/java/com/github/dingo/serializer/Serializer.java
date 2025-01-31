package com.github.dingo.serializer;

import java.io.IOException;

import com.github.dingo.Packet;

/**
 * 
 * @Description: Serializer
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public interface Serializer {

    byte[] serialize(Packet packet) throws IOException;

    Packet deserialize(byte[] bytes) throws IOException;

}
