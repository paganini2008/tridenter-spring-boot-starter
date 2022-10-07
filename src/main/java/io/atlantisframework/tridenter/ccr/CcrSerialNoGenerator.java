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
package io.atlantisframework.tridenter.ccr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.collection.MapUtils;

/**
 * 
 * CcrSerialNoGenerator
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public class CcrSerialNoGenerator {

	private final Map<String, RedisAtomicLong> serials = new ConcurrentHashMap<String, RedisAtomicLong>();

	private final String redisPathPrefix;
	private final RedisConnectionFactory connectionFactory;

	public CcrSerialNoGenerator(String redisPathPrefix, RedisConnectionFactory connectionFactory) {
		this.redisPathPrefix = redisPathPrefix;
		this.connectionFactory = connectionFactory;
	}

	public long nextSerialNo(String name) {
		final String redisCounterName = getRedisCounterName(name);
		RedisAtomicLong counter = MapUtils.get(serials, name, () -> {
			return new RedisAtomicLong(redisCounterName, connectionFactory);
		});
		return counter.incrementAndGet();
	}

	public long currentSerialNo(String name) {
		final String redisCounterName = getRedisCounterName(name);
		return MapUtils.get(serials, name, () -> {
			return new RedisAtomicLong(redisCounterName, connectionFactory);
		}).get();
	}

	public void clean(String name) {
		serials.remove(name);
	}

	protected String getRedisCounterName(String name) {
		return redisPathPrefix + ":" + name;
	}

}
