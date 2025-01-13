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
