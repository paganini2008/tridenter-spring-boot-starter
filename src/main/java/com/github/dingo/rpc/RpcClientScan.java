package com.github.dingo.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 
 * @Description: RpcClientScan
 * @Author: Fred Feng
 * @Date: 07/01/2025
 * @Version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({RpcClientScannerRegistrar.class})
public @interface RpcClientScan {

    String[] basePackages();
}
