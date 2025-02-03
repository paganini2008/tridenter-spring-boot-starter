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
package com.github.dingo.netty;

import com.github.dingo.MessageCodecFactory;
import com.github.dingo.serializer.KryoSerializer;
import com.github.dingo.serializer.Serializer;
import io.netty.channel.ChannelHandler;

/**
 * 
 * @Description: NettyMessageCodecFactory
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class NettyMessageCodecFactory implements MessageCodecFactory {

    private final Serializer serializer;

    public NettyMessageCodecFactory() {
        this(new KryoSerializer());
    }

    public NettyMessageCodecFactory(Serializer serializer) {
        this.serializer = serializer;
    }

    public ChannelHandler getEncoder() {
        return new NettyEncoderDecoderUtils.PacketEncoder(serializer);
    }

    public ChannelHandler getDecoder() {
        return new NettyEncoderDecoderUtils.PacketDecoder(serializer);
    }

}
