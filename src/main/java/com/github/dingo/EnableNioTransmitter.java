package com.github.dingo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import com.github.dingo.rpc.RpcAutoConfiguration;

/**
 * 
 * @Description: EnableNioTransmitter
 * @Author: Fred Feng
 * @Date: 29/12/2024
 * @Version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({NioTransmitterAutoConfiguration.class, RpcAutoConfiguration.class})
public @interface EnableNioTransmitter {

}
