package com.github.doodler.common.transmitter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @Description: RoundRobinPartitioner
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class RoundRobinPartitioner implements Partitioner {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public <T> T selectChannel(Object data, List<T> channels) {
        if (channels.size() == 1) {
            return channels.get(0);
        }
        int index = (int) (counter.getAndIncrement() & 0x7FFFFFFF % channels.size());
        return channels.get(index);
    }

    @Override
    public String getName() {
        return "roundrobin";
    }
}
