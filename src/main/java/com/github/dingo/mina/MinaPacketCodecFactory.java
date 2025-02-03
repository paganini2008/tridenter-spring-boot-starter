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
package com.github.dingo.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import com.github.dingo.mina.MinaEncoderDecoderUtils.PacketDecoder;
import com.github.dingo.mina.MinaEncoderDecoderUtils.PacketEncoder;
import com.github.dingo.serializer.KryoSerializer;
import com.github.dingo.serializer.Serializer;

/**
 * 
 * @Description: MinaPacketCodecFactory
 * @Author: Fred Feng
 * @Date: 09/01/2025
 * @Version 1.0.0
 */
public class MinaPacketCodecFactory implements ProtocolCodecFactory {

    private final PacketEncoder encoder;
    private final PacketDecoder decoder;

    public MinaPacketCodecFactory() {
        this(new KryoSerializer());
    }

    public MinaPacketCodecFactory(Serializer serializer) {
        encoder = new PacketEncoder(serializer);
        decoder = new PacketDecoder(serializer);
    }

    public ProtocolEncoder getEncoder(IoSession session) {
        return encoder;
    }

    public ProtocolDecoder getDecoder(IoSession session) {
        return decoder;
    }

    public int getEncoderMaxObjectSize() {
        return encoder.getMaxObjectSize();
    }

    public void setEncoderMaxObjectSize(int maxObjectSize) {
        encoder.setMaxObjectSize(maxObjectSize);
    }

    public int getDecoderMaxObjectSize() {
        return decoder.getMaxObjectSize();
    }

    public void setDecoderMaxObjectSize(int maxObjectSize) {
        decoder.setMaxObjectSize(maxObjectSize);
    }


}
