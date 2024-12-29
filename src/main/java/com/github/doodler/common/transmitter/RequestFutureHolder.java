package com.github.doodler.common.transmitter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import com.github.doodler.common.utils.MapUtils;

/**
 * 
 * @Description: RequestFutureHolder
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
public abstract class RequestFutureHolder {

    private static final Map<String, CompletableFuture<Object>> futures = new ConcurrentHashMap<>();

    public static CompletableFuture<Object> getRequest(String requestId) {
        return MapUtils.getOrCreate(futures, requestId, () -> new CompletableFuture<Object>());
    }

    public static CompletableFuture<Object> removeRequest(String requestId) {
        return futures.remove(requestId);
    }

    public static int getRequestCount() {
        return futures.size();
    }

    public static void clearRequests() {
        futures.clear();
    }

}
