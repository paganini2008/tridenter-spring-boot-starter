package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: KeepAliveTimeoutException
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
public class KeepAliveTimeoutException extends RuntimeException {

    private static final long serialVersionUID = -3214862285809923018L;

    public KeepAliveTimeoutException() {
        super();
    }

    public KeepAliveTimeoutException(String msg) {
        super(msg);
    }

}
