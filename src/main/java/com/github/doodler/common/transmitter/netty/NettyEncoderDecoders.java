package com.github.doodler.common.transmitter.netty;

import java.io.IOException;
import java.util.List;
import com.github.doodler.common.transmitter.Packet;
import com.github.doodler.common.transmitter.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 
 * @Description: NettyEncoderDecoders
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public abstract class NettyEncoderDecoders {

    public static class PacketEncoder extends MessageToByteEncoder<Packet> {

        private final Serializer serializer;

        public PacketEncoder(Serializer serializer) {
            this.serializer = serializer;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, Packet input, ByteBuf out)
                throws Exception {
            if (input == null) {
                throw new EncoderException("Input could not be null");
            }
            byte[] data;
            try {
                data = serializer.serialize(input);
            } catch (IOException e) {
                throw new EncoderException(e.getMessage(), e);
            }
            out.writeInt(data.length);
            out.writeBytes(data);
        }

    }

    public static class PacketDecoder extends ByteToMessageDecoder {

        private final Serializer serializer;

        public PacketDecoder(Serializer serializer) {
            this.serializer = serializer;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
                throws Exception {
            if (in.readableBytes() < 4) {
                return;
            }
            in.markReaderIndex();
            int dataLength = in.readInt();
            if (dataLength < 4) {
                throw new EncoderException("Data length should be greater than 4: " + dataLength);
            }
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }

            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Packet packet;
            try {
                packet = serializer.deserialize(body);
            } catch (IOException e) {
                throw new DecoderException(e.getMessage(), e);
            }
            out.add(packet);
        }

    }

}
