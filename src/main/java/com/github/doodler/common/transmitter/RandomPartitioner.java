package com.github.doodler.common.transmitter;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 
 * @Description: RandomPartitioner
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class RandomPartitioner implements Partitioner {

    @Override
    public <T> T selectChannel(Object data, List<T> channels) {
        if (channels.size() == 1) {
            return channels.get(0);
        }
        return channels.get(ThreadLocalRandom.current().nextInt(channels.size()));
    }

    @Override
    public String getName() {
        return "random";
    }

}
