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

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Marker;
import com.github.doodler.common.utils.Markers;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: ChannelSwitcher
 * @Author: Fred Feng
 * @Date: 14/01/2025
 * @Version 1.0.0
 */
@Slf4j
public final class ChannelSwitcher {

    private static Marker marker = Markers.SYSTEM;
    private final List<SocketAddress> internal = new CopyOnWriteArrayList<>();
    private final Map<SocketAddress, Boolean> external = new ConcurrentHashMap<>();

    public void enableInternalChannel(SocketAddress socketAddress) {
        if (!internal.contains(socketAddress)) {
            if (internal.add(socketAddress)) {
                if (log.isInfoEnabled()) {
                    log.info(marker, "Add internal channel to socket address: {}", socketAddress);
                }
            }
        }
    }

    public void enableExternalChannel(SocketAddress socketAddress, boolean enabled) {
        if (!internal.contains(socketAddress)) {
            if (!external.containsKey(socketAddress) || !external.get(socketAddress)) {
                external.put(socketAddress, enabled);
                if (enabled) {
                    if (log.isInfoEnabled()) {
                        log.info(marker, "Add external channel to socket address: {}",
                                socketAddress);
                    }
                }
            }
        }
    }

    public void enableExternalChannels(boolean enabled) {
        for (Map.Entry<SocketAddress, Boolean> entry : external.entrySet()) {
            entry.setValue(enabled);
        }
        if (log.isInfoEnabled()) {
            log.info(marker, "{} all external channels", enabled ? "Enable" : "Disable");
        }
    }

    public boolean contains(SocketAddress socketAddress) {
        return internal.contains(socketAddress) || external.containsKey(socketAddress);
    }

    public void remove(SocketAddress socketAddress) {
        if (internal.remove(socketAddress) || external.remove(socketAddress)) {
            if (log.isInfoEnabled()) {
                log.info(marker, "Remove channel to socket address: {}", socketAddress);
            }
        }
    }

    public boolean canAccess(SocketAddress socketAddress) {
        if (internal.contains(socketAddress)) {
            return true;
        }
        return external.containsKey(socketAddress) && external.get(socketAddress);
    }

}
