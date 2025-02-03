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
