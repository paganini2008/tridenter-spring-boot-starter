/**
* Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.atlantisframework.tridenter.utils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.github.paganini2008.devtools.StringUtils;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

/**
 * 
 * RedisClientConfiguration
 *
 * @author Fred Feng
 * @since 2.0.3
 */
@SuppressWarnings("all")
@Setter
@Configuration
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnProperty(name = "atlantis.framework.redis.autoconfigure", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RedisClientConfiguration.RedisSettings.class)
@ConditionalOnMissingBean(RedisConnectionFactory.class)
public class RedisClientConfiguration {

	/**
	 * 
	 * RedisSettings
	 *
	 * @author Fred Feng
	 *
	 * @since 2.0.3
	 */
	@ConfigurationProperties(prefix = "atlantis.framework.redis")
	@Getter
	@Setter
	public static class RedisSettings {

		private String host;
		private String password;
		private int port;
		private int database;

		private Map<String, String> settings = new HashMap<String, String>();

	}

	@ConditionalOnClass({ GenericObjectPool.class, Jedis.class })
	@Bean("redisConnectionFactory")
	public RedisConnectionFactory lettuceRedisConnectionFactory(RedisConfiguration redisConfiguration,
			GenericObjectPoolConfig redisPoolConfig) {
		JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfigurationBuilder = JedisClientConfiguration.builder();
		jedisClientConfigurationBuilder.connectTimeout(Duration.ofMillis(60000)).readTimeout(Duration.ofMillis(60000)).usePooling()
				.poolConfig(redisPoolConfig);
		if (redisConfiguration instanceof RedisStandaloneConfiguration) {
			return new JedisConnectionFactory((RedisStandaloneConfiguration) redisConfiguration, jedisClientConfigurationBuilder.build());
		} else if (redisConfiguration instanceof RedisSentinelConfiguration) {
			return new JedisConnectionFactory((RedisSentinelConfiguration) redisConfiguration, jedisClientConfigurationBuilder.build());
		} else if (redisConfiguration instanceof RedisClusterConfiguration) {
			return new JedisConnectionFactory((RedisClusterConfiguration) redisConfiguration, jedisClientConfigurationBuilder.build());
		}
		throw new UnsupportedOperationException("Create JedisConnectionFactory");
	}

	@ConditionalOnMissingBean(name = "redisPoolConfig")
	@Bean
	public GenericObjectPoolConfig redisPoolConfig() {
		GenericObjectPoolConfig redisPoolConfig = new GenericObjectPoolConfig();
		redisPoolConfig.setMinIdle(1);
		redisPoolConfig.setMaxIdle(10);
		redisPoolConfig.setMaxTotal(200);
		redisPoolConfig.setMaxWaitMillis(-1);
		redisPoolConfig.setTestWhileIdle(true);
		redisPoolConfig.setMinEvictableIdleTimeMillis(60000);
		redisPoolConfig.setTimeBetweenEvictionRunsMillis(30000);
		redisPoolConfig.setNumTestsPerEvictionRun(-1);
		return redisPoolConfig;
	}

	@ConditionalOnMissingBean(name = "redisConfiguration")
	@Bean
	public RedisConfiguration redisConfiguration(RedisSettings redisSettings) {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(redisSettings.getHost());
		redisStandaloneConfiguration.setPort(redisSettings.getPort());
		redisStandaloneConfiguration.setDatabase(redisSettings.getDatabase());
		if (StringUtils.isNotBlank(redisSettings.getPassword())) {
			redisStandaloneConfiguration.setPassword(RedisPassword.of(redisSettings.getPassword()));
		}
		return redisStandaloneConfiguration;
	}

	@ConditionalOnMissingBean(name = "stringRedisTemplate")
	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}

	@ConditionalOnMissingBean(name = "redisTemplate")
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		ObjectMapper om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
		jackson2JsonRedisSerializer.setObjectMapper(om);
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		redisTemplate.setKeySerializer(stringSerializer);
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
		redisTemplate.setHashKeySerializer(stringSerializer);
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

}
