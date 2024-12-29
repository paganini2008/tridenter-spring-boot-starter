package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: TransmitterServerException
 * @Author: Fred Feng
 * @Date: 30/12/2024
 * @Version 1.0.0
 */
public class TransmitterServerException extends TransmitterException {

    private static final long serialVersionUID = 2029521829545959153L;

    public TransmitterServerException() {
        super();
    }

    public TransmitterServerException(String msg) {
        super(msg);
    }

    public TransmitterServerException(Throwable e) {
        super(e);
    }

    public TransmitterServerException(String msg, Throwable e) {
        super(msg, e);
    }
}
