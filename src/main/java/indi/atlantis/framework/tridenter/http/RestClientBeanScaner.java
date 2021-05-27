package indi.atlantis.framework.tridenter.http;

import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * 
 * RestClientBeanScaner
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class RestClientBeanScaner extends ClassPathBeanDefinitionScanner {

	public RestClientBeanScaner(BeanDefinitionRegistry registry) {
		super(registry, false);
	}

	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		addIncludeFilter(new AnnotationTypeFilter(RestClient.class));
		Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
		if (beanDefinitionHolders.isEmpty()) {
			logger.warn("No RestClient interfaces was found in package '" + Arrays.toString(basePackages)
					+ "'. Please check your configuration again.");
		} else {
			processBeanDefinitions(beanDefinitionHolders);
		}
		return beanDefinitionHolders;
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
	}

	private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitionHolders) {
		GenericBeanDefinition beanDefinition;
		for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
			beanDefinition = ((GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition());
			beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
			beanDefinition.setBeanClass(RestClientProxyFactoryBean.class);
			beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
		}
	}

}
