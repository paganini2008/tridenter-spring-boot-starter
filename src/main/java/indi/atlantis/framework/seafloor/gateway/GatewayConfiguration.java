package indi.atlantis.framework.seafloor.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.cache.HashCache;

/**
 * 
 * GatewayConfiguration
 *
 * @author Jimmy Hoff
 * @version 1.0
 */
@Configuration
public class GatewayConfiguration {

	@Bean
	public RouterManager routerManager() {
		return new RouterManager();
	}

	@ConditionalOnMissingBean
	@Bean
	public EmbeddedServer embeddedServer() {
		return new NettyEmbeddedServer();
	}

	@Bean
	public RequestDispatcher dynamicRequestDispatcher() {
		return new DynamicRequestDispatcher();
	}

	@Bean
	public RequestDispatcher staticRequestDispatcher() {
		return new StaticRequestDispatcher();
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
