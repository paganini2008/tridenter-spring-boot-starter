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
    public <T> T selectChannel(Object obj, List<T> channels) {
        Packet packet = (Packet) obj;
        Object[] data = new Object[fieldNames.size()];
        int i = 0;
        for (String fieldName : fieldNames) {
            data[i++] = getFieldValue(packet, fieldName);
        }
        try {
            return channels.get(indexFor(packet, data, channels.size()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "hash";
    }

    protected Object getFieldValue(Packet packet, String fieldName) {
        return packet.getField(fieldName);
    }

    protected int indexFor(Packet packet, Object[] data, int length) {
        int hash = Arrays.deepHashCode(data);
        return (hash & 0x7FFFFFFF) % length;
    }

}
