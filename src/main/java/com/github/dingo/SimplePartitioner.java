package com.github.dingo;

import java.util.List;

/**
 * 
 * @Description: SimplePartitioner
 * @Author: Fred Feng
 * @Date: 14/01/2025
 * @Version 1.0.0
 */
public class SimplePartitioner implements Partitioner {

    public SimplePartitioner() {
        this(0);
    }

    public SimplePartitioner(int index) {
        this.index = index;
    }

    private final int index;

    @Override
    public <T> T selectChannel(Object data, List<T> channels) {
        int l;
        if ((l = channels.size()) > 0) {
            return channels.get(Math.max(index, l - 1));
        }
        return null;
    }

}
