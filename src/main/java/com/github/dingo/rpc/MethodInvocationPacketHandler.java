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
package com.github.dingo.rpc;

import com.github.dingo.Packet;
import com.github.dingo.PacketHandler;
import com.github.doodler.common.context.BeanReflectionService;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @Description: MethodInvocationPacketHandler
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
@RequiredArgsConstructor
public class MethodInvocationPacketHandler implements PacketHandler {

    private final BeanReflectionService beanReflectionService;

    @Override
    public Object handle(Packet packet) {
        String className = packet.getStringField("className");
        String beanName = packet.getStringField("beanName");
        String methodName = packet.getStringField("methodName");
        Object[] arguments = (Object[]) packet.getField("arguments");
        return beanReflectionService.invokeTargetMethod(className, beanName, methodName, arguments);
    }



}
