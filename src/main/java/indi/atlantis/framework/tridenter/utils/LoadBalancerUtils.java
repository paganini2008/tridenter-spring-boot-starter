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
package indi.atlantis.framework.tridenter.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.multithreads.AtomicIntegerSequence;

import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.LoadBalancer;

/**
 * 
 * LoadBalancerUtils
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public abstract class LoadBalancerUtils {

	/**
	 * 
	 * RoundRobinLoadBalancer
	 * 
	 * @author Fred Feng
	 *
	 * @since 2.0.1
	 */
	public static class RoundRobinLoadBalancer implements LoadBalancer {

		private final AtomicIntegerSequence counter = new AtomicIntegerSequence();

		public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
			if (CollectionUtils.isEmpty(candidates)) {
				return null;
			}
			return candidates.get(counter.getAndIncrement() % candidates.size());
		}

	}

	/**
	 * 
	 * RandomLoadBalancer
	 * 
	 * @author Fred Feng
	 *
	 * @since 2.0.1
	 */
	public static class RandomLoadBalancer implements LoadBalancer {

		public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
			if (CollectionUtils.isEmpty(candidates)) {
				return null;
			}
			return candidates.get(ThreadLocalRandom.current().nextInt(0, candidates.size()));
		}

	}

	/**
	 * 
	 * HashLoadBalancer
	 * 
	 * @author Fred Feng
	 *
	 * @since 2.0.1
	 */
	public static class HashLoadBalancer implements LoadBalancer {

		public ApplicationInfo select(Object message, List<ApplicationInfo> candidates) {
			if (CollectionUtils.isEmpty(candidates)) {
				return null;
			}
			int hash = message != null ? message.hashCode() : 0;
			hash &= 0x7FFFFFFF;
			return candidates.get(hash % candidates.size());
		}

	}

}
