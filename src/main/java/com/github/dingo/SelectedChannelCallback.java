package com.github.dingo;

/**
 * 
 * @Description: SelectedChannelCallback
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public interface SelectedChannelCallback {

    <T> T doSelectChannel(ChannelContext<T> channelContext);

}
