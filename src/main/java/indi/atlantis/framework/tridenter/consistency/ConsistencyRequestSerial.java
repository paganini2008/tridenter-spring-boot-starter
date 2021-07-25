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
package indi.atlantis.framework.tridenter.consistency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.springdessert.reditools.common.TtlKeeper;

import indi.atlantis.framework.tridenter.Constants;
import indi.atlantis.framework.tridenter.InstanceId;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsistencyRequestSerial
 *
 * @author Fred Feng
 * @since 1.0
 */
@Slf4j
public class ConsistencyRequestSerial {

	private static final String CONSISTENCY_SERIAL_PATTERN = "%s:consistency:serial:%s";
	private final Map<String, RedisAtomicLong> serials = new ConcurrentHashMap<String, RedisAtomicLong>();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private RedisConnectionFactory connectionFactory;

	@Autowired
	private TtlKeeper ttlKeeper;

	public long nextSerial(String name) {
		final String redisCounterName = counterName(name);
		try {
			RedisAtomicLong counter = MapUtils.get(serials, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keepAlive(l.getKey(), 5);
				return l;
			});
			return instanceId.getWeight() > 1 ? counter.incrementAndGet() + instanceId.getWeight() : counter.incrementAndGet();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			serials.remove(name);
			return nextSerial(name);
		}
	}

	public long currentSerial(String name) {
		final String redisCounterName = counterName(name);
		try {
			return MapUtils.get(serials, name, () -> {
				RedisAtomicLong l = new RedisAtomicLong(redisCounterName, connectionFactory);
				ttlKeeper.keepAlive(l.getKey(), 5);
				return l;
			}).get();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			serials.remove(name);
			return currentSerial(name);
		}
	}

	public void clean(String name) {
		serials.remove(name);
	}

	private String counterName(String name) {
		return String.format(CONSISTENCY_SERIAL_PATTERN, Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName, name);
	}

}
