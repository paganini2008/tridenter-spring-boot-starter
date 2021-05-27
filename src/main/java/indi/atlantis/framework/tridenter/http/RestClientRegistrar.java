package indi.atlantis.framework.tridenter.http;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * 
 * RestClientRegistrar
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestClientRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

	private final BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();
	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes annotationAttributes = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRestClient.class.getName()));
		List<String> basePackages = new ArrayList<String>();
		if (annotationAttributes.containsKey("basePackages")) {
			for (String basePackage : annotationAttributes.getStringArray("basePackages")) {
				if (StringUtils.hasText(basePackage)) {
					basePackages.add(basePackage);
				}
			}
		}
		if (basePackages.size() > 0) {
			RestClientBeanScaner scanner = new RestClientBeanScaner(registry);
			if (resourceLoader != null) {
				scanner.setResourceLoader(resourceLoader);
			}
			scanner.scan(StringUtils.toStringArray(basePackages));
		}
		if (annotationAttributes.containsKey("include")) {
			for (Class<?> clz : annotationAttributes.getClassArray("include")) {
				BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clz);
				GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanDefinitionBuilder.getBeanDefinition();
				beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
				beanDefinition.setBeanClass(RestClientProxyFactoryBean.class);
				beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
				String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
				registry.registerBeanDefinition(beanName, beanDefinition);
			}
		}

	}

}
