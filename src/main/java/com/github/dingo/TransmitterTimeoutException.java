package com.github.dingo;

/**
 * 
 * @Description: TransmitterTimeoutException
 * @Author: Fred Feng
 * @Date: 22/01/2025
 * @Version 1.0.0
 */
public class TransmitterTimeoutException extends TransmitterClientException {

    private static final long serialVersionUID = 4163996263478144377L;

    public TransmitterTimeoutException(String msg) {
        super(msg);
    }

}
