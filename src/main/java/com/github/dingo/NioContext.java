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

import java.util.concurrent.atomic.AtomicInteger;
import com.github.doodler.common.events.Context;

/**
 * 
 * @Description: NioContext
 * @Author: Fred Feng
 * @Date: 28/01/2025
 * @Version 1.0.0
 */
public final class NioContext extends Context {

    private final AtomicInteger concurrents = new AtomicInteger(0);

    public AtomicInteger getConcurrents() {
        return concurrents;
    }

}
