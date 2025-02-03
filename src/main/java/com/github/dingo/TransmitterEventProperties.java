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

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * 
 * @Description: TransmitterEventProperties
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@Data
@ConfigurationProperties("doodler.transmitter.event")
public class TransmitterEventProperties {

    private int maxBufferCapacity = 256;
    private int requestFetchSize = 10;
    private long timeout = 1000L;
    private long bufferCleanInterval = 3L * 1000;
    private boolean bufferCleanerEnabled = true;

    private boolean failureRetryEnabled = false;
    private int failureRetryInterval = 5;
    private int failureRetryTimes = 10;

    private InMemoryBuffer memory = new InMemoryBuffer();
    private RedisBuffer redis = new RedisBuffer();

    @Data
    public static class InMemoryBuffer {

        private int maxSize = 256;

    }

    @Data
    public static class RedisBuffer {

        private String namespace = "default";

    }

}
