package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: TransmitterClientException
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public class TransmitterClientException extends TransmitterException {

    private static final long serialVersionUID = -8928243837944163638L;

    public TransmitterClientException() {
        super();
    }

    public TransmitterClientException(String msg) {
        super(msg);
    }

    public TransmitterClientException(Throwable e) {
        super(e);
    }

    public TransmitterClientException(String msg, Throwable e) {
        super(msg, e);
    }

}
