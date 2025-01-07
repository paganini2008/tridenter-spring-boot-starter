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
    private long timeout = 100L;
    private long bufferCleanInterval = 5L * 1000;

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
