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
package indi.atlantis.framework.tridenter.http;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.devtools.collection.ListUtils;
import com.github.paganini2008.devtools.collection.MultiMappedMap;

/**
 * 
 * AbstractStatisticIndicator
 *
 * @author Fred Feng
 * @since 2.0.1
 */
public abstract class AbstractStatisticIndicator implements StatisticIndicator, Runnable {

	protected final MultiMappedMap<String, String, Statistic> cache = new MultiMappedMap<String, String, Statistic>();
	protected final PathMatcher pathMatcher = new AntPathMatcher();
	protected final List<String> pathPatterns = new CopyOnWriteArrayList<String>();

	@Autowired
	public void configure(@Qualifier("applicationClusterTaskScheduler") TaskScheduler taskScheduler) {
		taskScheduler.scheduleWithFixedDelay(this, Duration.ofSeconds(1));
	}

	public void includePath(String... pathPatterns) {
		if (ArrayUtils.isNotEmpty(pathPatterns)) {
			this.pathPatterns.addAll(Arrays.asList(pathPatterns));
		}
	}

	@Override
	public Statistic compute(String provider, Request request) {
		String path;
		if (shouldCompute(provider, request)) {
			path = request.getPath();
		} else {
			path = "/**";
		}
		Statistic statistic = cache.get(provider, path);
		if (statistic == null) {
			int maxPermits = request instanceof ForwardedRequest ? ((ForwardedRequest) request).getAllowedPermits() : Integer.MAX_VALUE;
			cache.putIfAbsent(provider, path, new Statistic(provider, path, maxPermits));
			statistic = cache.get(provider, path);
		}
		return statistic;
	}

	protected boolean shouldCompute(String provider, Request request) {
		for (String pathPattern : pathPatterns) {
			if (pathMatcher.match(pathPattern, request.getPath())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<Statistic> toCollection(String provider) {
		return cache.containsKey(provider) ? Collections.unmodifiableCollection((cache.get(provider).values())) : ListUtils.emptyList();
	}

	@Override
	public Map<String, Collection<Statistic>> toEntries() {
		Map<String, Collection<Statistic>> copy = new HashMap<String, Collection<Statistic>>();
		for (Map.Entry<String, Map<String, Statistic>> entry : cache.entrySet()) {
			copy.put(entry.getKey(), Collections.unmodifiableCollection(entry.getValue().values()));
		}
		return copy;
	}

	@Override
	public void run() {
		for (Map.Entry<String, Map<String, Statistic>> outter : cache.entrySet()) {
			for (Map.Entry<String, Statistic> inner : outter.getValue().entrySet()) {
				inner.getValue().calculateQps();
			}
		}
	}

}
