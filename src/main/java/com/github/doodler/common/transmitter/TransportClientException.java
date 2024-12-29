package com.github.doodler.common.transmitter;

/**
 * 
 * @Description: TransportClientException
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public class TransportClientException extends TransmitterException {

    private static final long serialVersionUID = -8928243837944163638L;

    public TransportClientException() {
        super();
    }

    public TransportClientException(String msg) {
        super(msg);
    }

    public TransportClientException(Throwable e) {
        super(e);
    }

    public TransportClientException(String msg, Throwable e) {
        super(msg, e);
    }

}
