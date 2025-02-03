/*
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dingo.serializer;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dingo.Packet;

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
