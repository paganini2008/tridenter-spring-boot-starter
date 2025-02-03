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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.github.doodler.common.events.Buffer;
import com.github.doodler.common.utils.BoundedList;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: InMemoryBuffer
 * @Author: Fred Feng
 * @Date: 26/12/2024
 * @Version 1.0.0
 */
@Slf4j
public class InMemoryBuffer<T> implements Buffer<T>, BoundedList.RemovalListener<T> {

    private final Buffer<T> overflowBuffer;

    private final BoundedList<T> cache;

    public InMemoryBuffer(int maxSize) {
        this(maxSize, null);
    }

    public InMemoryBuffer(int maxSize, Buffer<T> overflowBuffer) {
        this.overflowBuffer = overflowBuffer;
        this.cache = new BoundedList<T>(maxSize, this);
    }

    @Override
    public void put(T item) {
        cache.add(item);
    }

    @Override
    public long size() {
        long n = cache.size();
        if (overflowBuffer != null) {
            n += overflowBuffer.size();
        }
        return n;
    }

    @Override
    public T poll() {
        if (overflowBuffer != null && overflowBuffer.size() > 0) {
            return overflowBuffer.poll();
        }
        return cache.size() > 1 ? cache.remove(0) : null;
    }

    @Override
    public Collection<T> poll(long fetchSize) {
        if (fetchSize == 1) {
            return Collections.singletonList(poll());
        }
        if (overflowBuffer != null && overflowBuffer.size() > 0) {
            return overflowBuffer.poll(fetchSize);
        }
        List<T> sublist =
                new ArrayList<>(cache.subList(0, Math.min((int) fetchSize, cache.size())));
        cache.removeAll(sublist);
        return sublist;
    }

    @Override
    public void onRemoval(T elderValue) {
        if (overflowBuffer == null) {
            if (log.isInfoEnabled()) {
                log.info("Discard item: {}", elderValue);
            }
            return;
        }
        overflowBuffer.put(elderValue);
    }

}
