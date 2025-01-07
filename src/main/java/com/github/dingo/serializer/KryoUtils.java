
package com.github.dingo.serializer;

import static org.apache.commons.io.IOUtils.closeQuietly;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.github.dingo.Packet;
import lombok.experimental.UtilityClass;

/**
 * 
 * @Description: KryoUtils
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@UtilityClass
public class KryoUtils {

    public static final int DEFAULT_BUFFER_SIZE = 64;
    public static final int DEFAULT_POOL_SIZE = 256;

    private static final List<Class<?>> registeredClasses = new ArrayList<>();

    static {
        registeredClasses.add(Packet.class);
        registeredClasses.add(Object[].class);
    }

    private static boolean registrationRequired = false;

    public static void setRegistrationRequired(boolean registrationRequired) {
        KryoUtils.registrationRequired = registrationRequired;
    }

    public void registerClass(Class<?> type) {
        registeredClasses.add(type);
    }

    private static final ThreadLocal<Kryo> KRYOS = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        registeredClasses.forEach(c -> {
            kryo.register(c);
        });
        kryo.setReferences(false);
        kryo.setRegistrationRequired(registrationRequired);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    });


    private static Pool<Kryo> pool = new Pool<Kryo>(true, false, DEFAULT_POOL_SIZE) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            registeredClasses.forEach(c -> {
                kryo.register(c);
            });
            kryo.setReferences(false);
            kryo.setRegistrationRequired(registrationRequired);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    public <T> byte[] serializeToBytes(T t) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)) {
            serialize(t, os);
            os.flush();
            return os.toByteArray();
        }
    }

    public <T> T deserializeFromBytes(byte[] bytes, Class<T> c) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            return deserialize(is, c);
        }
    }

    public <T> void serializeToFile(T t, File f) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            serialize(t, fos);
            fos.flush();
        }
    }

    public <T> T deserializeFromFile(File f, Class<T> c) throws IOException {
        try (FileInputStream fis = new FileInputStream(f)) {
            return deserialize(fis, c);
        }
    }

    public <T> void serialize(T t, OutputStream os) {
        Output output = null;
        try {
            Kryo kryo = KRYOS.get();
            output = new Output(os);
            kryo.writeObject(output, t);
        } finally {
            closeQuietly(output);
        }
    }

    public <T> T deserialize(InputStream is, Class<T> c) {
        Input input = null;
        try {
            Kryo kryo = KRYOS.get();
            input = new Input(is);
            return kryo.readObject(input, c);
        } finally {
            closeQuietly(input);
        }
    }

    public <T> byte[] serializeToBytesWithPool(T t) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)) {
            serializeWithPool(t, os);
            os.flush();
            return os.toByteArray();
        }
    }

    public <T> T deserializeFromBytesWithPool(byte[] bytes, Class<T> c) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            return deserializeWithPool(is, c);
        }
    }

    public <T> void serializeWithPool(T t, OutputStream os) {
        Kryo kryo = pool.obtain();
        Output output = null;
        try {
            output = new Output(os);
            kryo.writeObject(output, t);
        } finally {
            closeQuietly(output);
            pool.free(kryo);
        }
    }

    public <T> T deserializeWithPool(InputStream is, Class<T> c) {
        Kryo kryo = pool.obtain();
        Input input = null;
        try {
            input = new Input(is);
            return kryo.readObject(input, c);
        } finally {
            closeQuietly(input);
            pool.free(kryo);
        }
    }

    public static void main(String[] args) throws Exception {
        Packet packet = Packet.PING;
        System.out.println(packet);
        byte[] bytes = serializeToBytesWithPool(packet);
        System.out.println(bytes.length);
        Packet other = deserializeFromBytesWithPool(bytes, Packet.class);
        System.out.println(other.equals(packet));
    }



}
