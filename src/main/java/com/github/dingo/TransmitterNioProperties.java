package com.github.dingo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * 
 * @Description: TransmitterNioProperties
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
@ConfigurationProperties("doodler.transmitter.nio")
@Data
public class TransmitterNioProperties {

    private NioClient client = new NioClient();
    private NioServer server = new NioServer();
    private boolean defaultExternalChannelAccessable = true;

    @Data
    public static class NioClient {
        private int threadCount = -1;
        private int connectionTimeout = 60 * 1000;
        private int senderBufferSize = 1024 * 1024;
        private int readerIdleTimeout = 0;
        private int writerIdleTimeout = 45;
        private int allIdleTimeout = 0;
        private boolean keepAlive = true;
        private boolean connectWithSelf = true;
        private int reconnectInterval = 6;
        private int maxReconnectAttempts = 10;
    }

    @Data
    public static class NioServer {
        private int threadCount = -1;
        private String bindHostName = "127.0.0.1";
        private int backlog = 128;
        private int readerBufferSize = 2 * 1024 * 1024;
        private int receiverBufferSize = 2 * 1024 * 1024;
        private int readerIdleTimeout = 60;
        private int writerIdleTimeout = 0;
        private int allIdleTimeout = 0;
        private boolean keepAlive = true;
    }


}
