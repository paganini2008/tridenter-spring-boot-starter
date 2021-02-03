package org.springtribe.framework.cluster.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.cache.HashCache;

/**
 * 
 * CustomizedRoutingConfig
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Configuration
public class CustomizedRoutingConfig {

	@Bean
	public RouterManager routerManager() {
		return new RouterManager();
	}

	@ConditionalOnMissingBean
	@Bean
	public EmbeddedServer embeddedServer() {
		return new NettyEmbeddedServer();
	}

	@ConditionalOnMissingBean
	@Bean
	public RequestDispatcher requestDispatcher() {
		return new NettyRequestDispatcher();
	}

	@ConditionalOnMissingBean
	@Bean
	public Cache remoteCache() {
		return new HashCache().expiredCache(3);
	}

	@ConditionalOnMissingBean
	@Bean
	public RouterCustomizer routerCustomizer() {
		return new DefaultRouterCustomizer();
	}

}
