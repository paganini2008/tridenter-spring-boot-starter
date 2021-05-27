package indi.atlantis.framework.tridenter.gateway;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.paganini2008.devtools.cache.Cache;
import com.github.paganini2008.devtools.cache.LruCache;

/**
 * 
 * GatewayAutoConfiguration
 *
 * @author Fred Feng
 * @version 1.0
 */
@Configuration
public class GatewayAutoConfiguration {

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
	public HttpRequestDispatcher httpRequestDispatcher() {
		return new HttpRequestDispatcher();
	}

	@Bean
	public ResourceResolver dynamicResourceResolver() {
		return new DynamicResourceResolver();
	}

	@Bean
	public ResourceResolver staticResourceResolver() {
		return new StaticResourceResolver();
	}

	@ConditionalOnMissingBean(name = "staticResourceCache")
	@Bean
	public Cache staticResourceCache() {
		LruCache cache = new LruCache(256) {

			@Override
			protected void dispose(Object eldestKey, Object eldestObject) {
				File file = (File) ((CachedResponseEntity) eldestObject).getBody();
				if (file.exists()) {
					file.delete();
				}
			}

		};
		return cache.expiredCache(3, TimeUnit.MINUTES);
	}

	@ConditionalOnMissingBean(name = "dynamicResourceCache")
	@Bean
	public Cache dynamicResourceCache() {
		LruCache cache = new LruCache(256);
		return cache.expiredCache(3, TimeUnit.MINUTES);
	}

	@ConditionalOnMissingBean
	@Bean
	public RouterCustomizer routerCustomizer() {
		return new DefaultRouterCustomizer();
	}

}
