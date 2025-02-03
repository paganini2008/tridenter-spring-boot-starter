/*
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public DataAccessTransmitterClientException(String msg) {
        super(msg);
        this.detail = "";
    }

    public DataAccessTransmitterClientException(String msg, String detail) {
        super(msg);
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }


}
