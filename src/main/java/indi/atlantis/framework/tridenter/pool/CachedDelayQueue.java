/**
* Copyright 2021 Fred Feng (paganini.fy@gmail.com)

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
package indi.atlantis.framework.tridenter.pool;

import java.util.concurrent.RejectedExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import com.github.paganini2008.devtools.multithreads.ThreadUtils;
import com.github.paganini2008.springdessert.reditools.RedisComponentNames;

import indi.atlantis.framework.tridenter.Constants;

/**
 * 
 * CachedDelayQueue
 *
 * @author Fred Feng
 * @version 1.0
 */
public class CachedDelayQueue implements DelayQueue {

	@Autowired
	@Qualifier(RedisComponentNames.REDIS_TEMPLATE)
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${spring.application.cluster.pool.pending-queue.maxSize:-1}")
	private int queueMaxSize;

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	public void offer(Invocation invocation) {
		String key = getKey();
		long queueSize = redisTemplate.opsForList().size(key);
		if (queueMaxSize == -1 || queueSize <= queueMaxSize) {
			redisTemplate.opsForList().leftPush(key, invocation);
		} else {
			throw new RejectedExecutionException("Pool pending queue has been full. Current size is " + queueSize);
		}
	}

	public Invocation pop() {
		return (Invocation) redisTemplate.opsForList().leftPop(getKey());
	}

	public void waitForTermination() {
		while (redisTemplate.opsForList().size(getKey()) != 0) {
			ThreadUtils.randomSleep(1000L);
		}
	}

	public int size() {
		Number result = redisTemplate.opsForList().size(getKey());
		return result != null ? result.intValue() : 0;
	}

	private String getKey() {
		return Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":pool:delay-queue";
	}

}
