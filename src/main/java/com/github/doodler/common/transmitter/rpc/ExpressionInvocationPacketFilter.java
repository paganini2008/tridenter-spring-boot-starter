package com.github.doodler.common.transmitter.rpc;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import com.github.doodler.common.context.ApplicationContextUtils;
import com.github.doodler.common.transmitter.Packet;
import com.github.doodler.common.transmitter.PacketFilter;
import lombok.Setter;

/**
 * 
 * @Description: ExpressionInvocationPacketFilter
 * @Author: Fred Feng
 * @Date: 03/01/2025
 * @Version 1.0.0
 */
public class ExpressionInvocationPacketFilter implements PacketFilter, BeanFactoryAware {

    @Setter
    private BeanFactory beanFactory;

    @Override
    public Object doFilter(Packet packet) {
        String spel = packet.getStringField("spel");
        if (StringUtils.isBlank(spel)) {
            return null;
        }
        ConfigurableListableBeanFactory beanFactory =
                (ConfigurableListableBeanFactory) ApplicationContextUtils.getBeanFactory();
        ConfigurableListableBeanFactory cbf = (ConfigurableListableBeanFactory) beanFactory;
        return cbf.getBeanExpressionResolver().evaluate(spel, new BeanExpressionContext(cbf, null));
    }

}
