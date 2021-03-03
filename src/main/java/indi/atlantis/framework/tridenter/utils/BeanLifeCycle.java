package indi.atlantis.framework.tridenter.utils;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * BeanLifeCycle
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public interface BeanLifeCycle extends InitializingBean, DisposableBean {

	default void configure() throws Exception {
	}

	@Override
	default void destroy() {
	}

	@Override
	default void afterPropertiesSet() throws Exception {
		configure();
	}

}
