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
package com.github.dingo.grizzly;

import com.github.dingo.grizzly.GrizzlyEncoderDecoderUtils.PacketDecoder;
import com.github.dingo.grizzly.GrizzlyEncoderDecoderUtils.PacketEncoder;
import com.github.dingo.serializer.KryoSerializer;
import com.github.dingo.serializer.Serializer;

/**
 * 
 * @Description: GrizzlyPacketCodecFactory
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public class GrizzlyPacketCodecFactory implements PacketCodecFactory {

    private final Serializer serializer;

    public GrizzlyPacketCodecFactory() {
        this(new KryoSerializer());
    }

    public GrizzlyPacketCodecFactory(Serializer serializer) {
        this.serializer = serializer;
    }

    public PacketEncoder getEncoder() {
        return new PacketEncoder(serializer);
    }

    public PacketDecoder getDecoder() {
        return new PacketDecoder(serializer);
    }

    public Serializer getSerializer() {
        return serializer;
    }

}
