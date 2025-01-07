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
