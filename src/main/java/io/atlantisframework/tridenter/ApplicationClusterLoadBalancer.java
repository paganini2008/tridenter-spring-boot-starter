/**
* Copyright 2017-2021 Fred Feng (paganini.fy@gmail.com)

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
package io.atlantisframework.tridenter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.StringUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.reditools.common.RedisAtomicLongSequence;

/**
 * 
 * ApplicationClusterLoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class ApplicationClusterLoadBalancer implements LoadBalancer {

	private final RedisAtomicLongSequence counter;
	private final Map<String, RedisAtomicLongSequence> counterSelector;
	private final RedisConnectionFactory redisConnectionFactory;

	public ApplicationClusterLoadBalancer(String name, RedisConnectionFactory redisConnectionFactory) {
		this.counter = new RedisAtomicLongSequence(name, redisConnectionFactory);
		this.counterSelector = new ConcurrentHashMap<String, RedisAtomicLongSequence>();
		this.redisConnectionFactory = redisConnectionFactory;
	}

	@Override
	public ApplicationInfo select(String group, List<ApplicationInfo> candidates, Object message) {
		if (CollectionUtils.isEmpty(candidates)) {
			return null;
		}
		return candidates.get((int) (getCounter(group).getAndIncrement() % candidates.size()));
	}

	private RedisAtomicLongSequence getCounter(final String group) {
		if (StringUtils.isBlank(group)) {
			return counter;
		}
		return MapUtils.get(counterSelector, group, () -> {
			return new RedisAtomicLongSequence(counter.getName() + ":" + group, redisConnectionFactory);
		});
	}

}
