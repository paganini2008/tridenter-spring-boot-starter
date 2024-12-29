package com.github.doodler.common.transmitter;

import io.netty.channel.ChannelHandler;

/**
 * 
 * @Description: MessageCodecFactory
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public interface MessageCodecFactory {

    ChannelHandler getEncoder();

    ChannelHandler getDecoder();

}
