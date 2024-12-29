package com.github.doodler.common.transmitter;

/**
 * 
 * KeepAliveTimeoutException
 *
 * @author Fred Feng
 * @since 2.0.1
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
