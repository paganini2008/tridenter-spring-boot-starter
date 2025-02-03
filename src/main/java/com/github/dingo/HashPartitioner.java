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
package com.github.dingo;

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
