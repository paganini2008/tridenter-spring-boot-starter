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

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;
import com.github.dingo.Packet;
import com.github.dingo.serializer.KryoSerializer;
import com.github.dingo.serializer.Serializer;

/**
 * 
 * @Description: PacketFilter
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
public class PacketFilter extends AbstractCodecFilter<Buffer, Packet> {

    public PacketFilter() {
        this(new KryoSerializer());
    }

    public PacketFilter(Serializer serializer) {
        this(new GrizzlyPacketCodecFactory(serializer));
    }

    public PacketFilter(PacketCodecFactory codecFactory) {
        super(codecFactory.getDecoder(), codecFactory.getEncoder());
    }

}
