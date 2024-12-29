package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: TransmitterException
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public class TransmitterException extends RuntimeException {

    private static final long serialVersionUID = -4607629563732835139L;

    public TransmitterException() {
        super();
    }

    public TransmitterException(String msg) {
        super(msg);
    }

    public TransmitterException(Throwable e) {
        super(e);
    }

    public TransmitterException(String msg, Throwable e) {
        super(msg, e);
    }

}
