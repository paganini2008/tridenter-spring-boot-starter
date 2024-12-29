package com.github.doodler.common.transmitter;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @Description: NioClient
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface NioClient extends LifeCycle, NioConnection, Client {

    void watchConnection(int checkInterval, TimeUnit timeUnit);

}
