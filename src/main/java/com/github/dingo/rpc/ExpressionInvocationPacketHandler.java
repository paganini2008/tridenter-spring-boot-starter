package com.github.dingo.rpc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.github.dingo.Packet;
import com.github.dingo.PacketHandler;

import lombok.Setter;

/**
 * 
 * @Description: ExpressionInvocationPacketHandler
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public class ExpressionInvocationPacketHandler implements PacketHandler, BeanFactoryAware {

    @Setter
    private BeanFactory beanFactory;

    @Override
    public Object handle(Packet packet) {
        String spel = packet.getStringField("spel");
        if (StringUtils.isBlank(spel)) {
            return null;
        }
        ConfigurableListableBeanFactory cbf = (ConfigurableListableBeanFactory) beanFactory;
        return cbf.getBeanExpressionResolver().evaluate(spel, new BeanExpressionContext(cbf, null));
    }

}
