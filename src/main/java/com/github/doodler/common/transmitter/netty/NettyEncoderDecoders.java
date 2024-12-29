package com.github.doodler.common.transmitter.netty;

import java.util.List;
import com.github.doodler.common.transmitter.Packet;
import com.github.doodler.common.transmitter.TransmitterClientException;
import com.github.doodler.common.transmitter.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
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
                throw new TransmitterClientException("Input could not be null");
            }
            byte[] data = serializer.serialize(input);
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
                throw new TransmitterClientException(
                        "Data length should be greater than 4: " + dataLength);
            }
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }

            byte[] body = new byte[dataLength];
            in.readBytes(body);
            Packet packet = serializer.deserialize(body);
            out.add(packet);
        }

    }

}
