package com.github.dingo;

/**
 * 
 * @Description: DataAccessTransmitterClientException
 * @Author: Fred Feng
 * @Date: 04/01/2025
 * @Version 1.0.0
 */
public class DataAccessTransmitterClientException extends TransmitterClientException {

    private static final long serialVersionUID = -1207406794200638692L;

    private final String detail;

    public DataAccessTransmitterClientException(String msg, String detail) {
        super(msg);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }


}
