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
