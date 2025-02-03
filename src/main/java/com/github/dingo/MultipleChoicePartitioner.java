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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * 
 * @Description: MultipleChoicePartitioner
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public class MultipleChoicePartitioner implements Partitioner {

    private final Map<String, Partitioner> byNames = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Partitioner> byTopics = Collections.synchronizedMap(new HashMap<>());

    public MultipleChoicePartitioner() {
        addPartitioner(new RoundRobinPartitioner());
        addPartitioner(new RandomPartitioner());
    }

    private Partitioner defaultPartitioner = new RoundRobinPartitioner();

    public void setDefaultPartitioner(Partitioner defaultPartitioner) {
        Assert.isNull(defaultPartitioner, "Default partitioner must not be null.");
        this.defaultPartitioner = defaultPartitioner;
    }

    public void addPartitioner(Partitioner partitioner) {
        Assert.notNull(partitioner, "Partitioner must not be null.");
        byNames.put(partitioner.getName(), partitioner);
    }

    public void removePartitioner(Partitioner partitioner) {
        Assert.notNull(partitioner, "Partitioner must not be null.");
        byNames.remove(partitioner.getName());
    }

    public void addPartitioner(String topic, Partitioner partitioner) {
        Assert.notNull(partitioner, "Partitioner must not be null.");
        byTopics.put(topic, partitioner);
    }

    public void removePartitioner(String topic) {
        byTopics.remove(topic);
    }

    @Override
    public <T> T selectChannel(Object data, List<T> channels) {
        Partitioner partitioner = null;
        if (data instanceof Packet) {
            Packet packet = (Packet) data;
            partitioner = byTopics.get(packet.getTopic());
            if (partitioner == null) {
                partitioner = byNames.get(packet.getPartitioner());
            }
        }
        if (partitioner == null) {
            partitioner = defaultPartitioner;
        }
        return partitioner.selectChannel(data, channels);
    }

}
