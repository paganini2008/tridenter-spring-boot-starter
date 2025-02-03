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

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: NioTemplate
 * @Author: Fred Feng
 * @Date: 07/01/2025
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class NioTemplate {

    private final NioClient nioClient;
    private final Partitioner partitioner;

    public void convertAndSendAllAsync(Object payload) {
        nioClient.send(payload);
    }

    public void convertAndSendAsync(Object payload) {
        nioClient.send(payload, partitioner);
    }

    public void convertAndSendAsync(Object payload, String serviceLocation) {
        nioClient.send(payload, serviceLocation);
    }

    public Object convertAndSend(Object payload) {
        return nioClient.sendAndReturn(payload, partitioner);
    }

    public Object convertAndSend(Object payload, long timeout, TimeUnit timeUnit) {
        return nioClient.sendAndReturn(payload, partitioner, timeout, timeUnit);
    }

    public Object convertAndSend(Object payload, String serviceLocation) {
        return nioClient.sendAndReturn(payload, serviceLocation);
    }

    public Object convertAndSend(Object payload, String serviceLocation, long timeout,
            TimeUnit timeUnit) {
        return nioClient.sendAndReturn(payload, serviceLocation, timeout, timeUnit);
    }

}
