package com.github.doodler.common.transmitter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 
 * @Description: HashPartitioner
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class HashPartitioner implements Partitioner {

    private final Set<String> fieldNames = new HashSet<>();

    public HashPartitioner(String... fieldNames) {
        addFields(fieldNames);
    }

    public void addFields(String... fieldNames) {
        if (ArrayUtils.isNotEmpty(fieldNames)) {
            this.fieldNames.addAll(Arrays.asList(fieldNames));
        }
    }

    @Override
    public <T> T selectChannel(Object message, List<T> channels) {
        if (channels.size() == 1) {
            return channels.get(0);
        }
        Object[] values = getValues(message);
        try {
            return channels.get(indexFor(message, values, channels.size()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "hash";
    }

    protected Object[] getValues(Object message) {
        Packet packet = (Packet) message;
        Object[] data = new Object[fieldNames.size()];
        int i = 0;
        for (String fieldName : fieldNames) {
            data[i++] = packet.getField(fieldName);
        }
        return data;
    }

    protected int indexFor(Object message, Object[] values, int length) {
        int hash = Arrays.deepHashCode(values);
        return (hash & 0x7FFFFFFF) % length;
    }

}
