package com.github.dingo.serializer;

/**
 * 
 * @Description: SerializationException
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = -2875891546096167730L;

    public SerializationException() {
        super();
    }

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(String msg, Throwable e) {
        super(msg, e);
    }

}
