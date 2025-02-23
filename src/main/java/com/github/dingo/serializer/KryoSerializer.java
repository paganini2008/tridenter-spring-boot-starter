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
