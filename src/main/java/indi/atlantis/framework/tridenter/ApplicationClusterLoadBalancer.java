/**
* Copyright 2018-2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter;

import java.util.List;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.springdessert.reditools.common.RedisAtomicLongSequence;

/**
 * 
 * ApplicationClusterLoadBalancer
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ApplicationClusterLoadBalancer implements LoadBalancer {

	private final RedisAtomicLongSequence counter;

	public ApplicationClusterLoadBalancer(String name, RedisConnectionFactory connectionFactory) {
		this.counter = new RedisAtomicLongSequence(name, connectionFactory);
	}

	@Override
	public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
		if (CollectionUtils.isEmpty(candidates)) {
			return null;
		}
		return candidates.get((int) (counter.getAndIncrement() % candidates.size()));
	}

}
