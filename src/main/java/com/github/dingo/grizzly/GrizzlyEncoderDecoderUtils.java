package com.github.dingo.grizzly;

import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.AttributeStorage;
import com.github.dingo.Packet;
import com.github.dingo.TransmitterClientException;
import com.github.dingo.serializer.Serializer;
import lombok.SneakyThrows;

/**
 * 
 * @Description: GrizzlyEncoderDecoderUtils
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
public abstract class GrizzlyEncoderDecoderUtils {

    public static class PacketDecoder extends AbstractTransformer<Buffer, Packet> {

        private final Serializer serializer;

        public PacketDecoder(Serializer serializer) {
            this.serializer = serializer;
        }

        @Override
        public String getName() {
            return "TupleDecoder";
        }

        @Override
        public boolean hasInputRemaining(AttributeStorage storage, Buffer input) {
            return input != null && input.hasRemaining();
        }

        @SneakyThrows
        @Override
        protected TransformationResult<Buffer, Packet> transformImpl(AttributeStorage storage,
                Buffer input) throws TransformationException {
            Integer objectSize = (Integer) storage.getAttributes().getAttribute("objectSize");
            if (objectSize == null) {
                if (input.remaining() < 4) {
                    return TransformationResult.createIncompletedResult(input);
                }
                objectSize = input.getInt();
                storage.getAttributes().setAttribute("objectSize", objectSize);
            }
            if (input.remaining() < objectSize) {
                return TransformationResult.createIncompletedResult(input);
            }
            final int limit = input.limit();
            input.limit(input.position() + objectSize);
            byte[] data = new byte[input.remaining()];
            input.get(data);
            Packet tuple = serializer.deserialize(data);

            input.position(input.limit());
            input.limit(limit);
            storage.getAttributes().removeAttribute("objectSize");
            return TransformationResult.createCompletedResult(tuple, input);
        }

    }

    public static class PacketEncoder extends AbstractTransformer<Packet, Buffer> {

        private final Serializer serializer;

        public PacketEncoder(Serializer serializer) {
            this.serializer = serializer;
        }

        @Override
        public String getName() {
            return "TupleEncoder";
        }

        @Override
        public boolean hasInputRemaining(AttributeStorage storage, Packet input) {
            return input != null;
        }

        @SneakyThrows
        @Override
        protected TransformationResult<Packet, Buffer> transformImpl(AttributeStorage storage,
                Packet input) throws TransformationException {
            if (input == null) {
                throw new TransmitterClientException("Input could not be null");
            }
            byte[] data = serializer.serialize(input);

            final Buffer output = obtainMemoryManager(storage).allocate(data.length + 4);
            output.putInt(data.length);
            output.put(data);
            output.flip();
            output.allowBufferDispose(true);

            return TransformationResult.createCompletedResult(output, null);
        }

    }

}
