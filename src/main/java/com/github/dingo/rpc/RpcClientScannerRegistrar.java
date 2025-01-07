package com.github.dingo.rpc;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * 
 * @Description: RpcClientScannerRegistrar
 * @Author: Fred Feng
 * @Date: 07/01/2025
 * @Version 1.0.0
 */
public class RpcClientScannerRegistrar
        implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(RpcClientScan.class.getName()));
        List<String> basePackages = new ArrayList<String>();
        if (annotationAttributes.containsKey("basePackages")) {
            for (String basePackage : annotationAttributes.getStringArray("basePackages")) {
                if (StringUtils.hasText(basePackage)) {
                    basePackages.add(basePackage);
                }
            }
        }
        ClassPathBeanDefinitionScanner scanner = new ClassPathRpcClientScanner(registry);
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        scanner.scan(StringUtils.toStringArray(basePackages));
    }
}
