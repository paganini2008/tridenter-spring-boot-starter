package com.github.dingo.grizzly;

import java.util.concurrent.TimeUnit;
import org.glassfish.grizzly.Connection;
import com.github.dingo.KeepAliveTimeoutException;
import com.github.dingo.Packet;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @Description: IdleTimeoutPolicies
 * @Author: Fred Feng
 * @Date: 08/01/2025
 * @Version 1.0.0
 */
@Slf4j
@SuppressWarnings("all")
public abstract class IdleTimeoutPolicies {

    public static IdleTimeoutFilter.TimeoutHandler NOOP = new IdleTimeoutFilter.TimeoutHandler() {

        public void onTimeout(Connection connection) {}

    };

    public static IdleTimeoutFilter.TimeoutHandler PING = new IdleTimeoutFilter.TimeoutHandler() {

        public void onTimeout(Connection connection) {
            connection.write(Packet.PING);
            throw new KeepAliveTimeoutException();
        }

    };

    public static IdleTimeoutFilter.TimeoutHandler READER_IDLE_LOG =
            new IdleTimeoutFilter.TimeoutHandler() {

                public void onTimeout(Connection connection) {
                    log.warn("[Reader Idle] Send a keep-alive message after {} second(s).",
                            connection.getReadTimeout(TimeUnit.SECONDS));
                    throw new KeepAliveTimeoutException();
                }

            };

    public static IdleTimeoutFilter.TimeoutHandler WRITER_IDLE_LOG =
            new IdleTimeoutFilter.TimeoutHandler() {

                public void onTimeout(Connection connection) {
                    log.warn("[Writer Idle] Send a keep-alive message after {} second(s).",
                            connection.getWriteTimeout(TimeUnit.SECONDS));
                    throw new KeepAliveTimeoutException();
                }

            };

    public static IdleTimeoutFilter.TimeoutHandler CLOSE = new IdleTimeoutFilter.TimeoutHandler() {

        public void onTimeout(Connection connection) {
            log.warn(
                    "Closing the channel because a keep-alive response "
                            + "message was not sent within {} second(s).",
                    connection.getWriteTimeout(TimeUnit.SECONDS));
            connection.closeSilently();
        }

    };

}
