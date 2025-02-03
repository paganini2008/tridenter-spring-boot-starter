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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import com.github.doodler.common.utils.NetUtils;
import com.github.doodler.common.utils.SingleObservable;
import com.github.doodler.common.utils.ThreadUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: NioConnectionKeeper
 * @Author: Fred Feng
 * @Date: 27/12/2024
 * @Version 1.0.0
 */
@Slf4j
public final class NioConnectionKeeper {

    private final int checkInterval;
    private final TimeUnit timeUnit;
    private final int maxAttempts;
    private final NioConnection connection;

    public NioConnectionKeeper(int checkInterval, TimeUnit timeUnit, int maxAttempts,
            NioConnection connection) {
        this.checkInterval = checkInterval;
        this.timeUnit = timeUnit;
        this.maxAttempts = maxAttempts;
        this.connection = connection;
    }

    private final SingleObservable observable = new SingleObservable(true);

    public void reconnect(SocketAddress remoteAddress) {
        String repr = NetUtils.toExternalString((InetSocketAddress) remoteAddress);
        if (observable.notifyObservers(repr, remoteAddress)) {
            log.info("Fire reconnection to '{}'", repr);
        }
    }

    public void keep(final SocketAddress remoteAddress, final HandshakeCallback callback) {
        String repr = NetUtils.toExternalString((InetSocketAddress) remoteAddress);
        boolean result = observable.addObserver(repr, (ob, arg) -> {
            int n = 0;
            do {
                if (log.isInfoEnabled()) {
                    log.info("Waiting for reconnecting to '{}'", remoteAddress);
                }
                ThreadUtils.sleep(checkInterval, timeUnit);
                try {
                    connection.connect((SocketAddress) arg, callback);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(e.getMessage(), e);
                    }
                }
            } while (!connection.isConnected(remoteAddress) && n++ < maxAttempts);
            if (!connection.isConnected((SocketAddress) arg)) {
                log.warn("Lost connection to '{}'", repr);
            }
        });
        if (result) {
            log.info(
                    "Keep watching connection to remote remoteAddress '{}' and will reconnect once exceptional disconnection",
                    repr);
        }
    }

}
