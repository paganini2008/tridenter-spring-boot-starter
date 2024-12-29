package com.github.doodler.common.transmitter;

import java.util.List;

/**
 * 
 * @Description: Partitioner
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public interface Partitioner {

    <T> T selectChannel(Object data, List<T> channels);

    default String getName() {
        return "";
    }

}
