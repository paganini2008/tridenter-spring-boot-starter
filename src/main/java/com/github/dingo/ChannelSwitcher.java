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
        internal.add(socketAddress);
        if (log.isInfoEnabled()) {
            log.info(marker, "Enable internal channel to socket address: {}", socketAddress);
        }
    }

    public void enableExternalChannel(SocketAddress socketAddress, boolean enabled) {
        external.put(socketAddress, enabled);
        if (log.isInfoEnabled()) {
            log.info(marker, "{} external channel to socket address: {}",
                    enabled ? "Enable" : "Disable", socketAddress);
        }
    }

    public void toggle(boolean enabled) {
        for (Map.Entry<SocketAddress, Boolean> entry : external.entrySet()) {
            entry.setValue(enabled);
        }
        if (log.isInfoEnabled()) {
            log.info(marker, "{} all external channels.", enabled ? "Enable" : "Disable");
        }
    }

    public void remove(SocketAddress socketAddress) {
        internal.remove(socketAddress);
        external.remove(socketAddress);
        if (log.isInfoEnabled()) {
            log.info(marker, "Remove channel to socket address: {}", socketAddress);
        }
    }

    public boolean canAccess(SocketAddress socketAddress) {
        if (internal.contains(socketAddress)) {
            return true;
        }
        return external.containsKey(socketAddress) && external.get(socketAddress);
    }

}
