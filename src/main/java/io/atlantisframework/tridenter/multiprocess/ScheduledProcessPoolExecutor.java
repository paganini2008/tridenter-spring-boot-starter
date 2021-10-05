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
package io.atlantisframework.tridenter.multiprocess;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.github.paganini2008.devtools.date.DateUtils;

/**
 * 
 * ScheduledProcessPoolExecutor
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public class ScheduledProcessPoolExecutor implements ScheduledProcessPool {

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private ProcessPool processPool;

	private final Map<Invocation, TaskPromise> cache = new ConcurrentHashMap<Invocation, TaskPromise>();

	@Override
	public TaskPromise schedule(Invocation invocation, Date startDate) {
		ScheduledFuture<?> future = taskScheduler.schedule(() -> {
			processPool.execute(invocation);
		}, startDate);
		cache.putIfAbsent(invocation, new ScheduledTaskPromise(future));
		return cache.get(invocation);
	}

	@Override
	public TaskPromise schedule(Invocation invocation, String cronExpression) {
		ScheduledFuture<?> future = taskScheduler.schedule(() -> {
			processPool.execute(invocation);
		}, new CronTrigger(cronExpression, TimeZone.getDefault()));
		cache.putIfAbsent(invocation, new ScheduledTaskPromise(future));
		return cache.get(invocation);
	}

	@Override
	public TaskPromise scheduleWithFixedDelay(Invocation invocation, Date startDate, long delay, TimeUnit timeUnit) {
		ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(() -> {
			processPool.execute(invocation);
		}, startDate, DateUtils.convertToMillis(delay, timeUnit));
		cache.putIfAbsent(invocation, new ScheduledTaskPromise(future));
		return cache.get(invocation);
	}

	@Override
	public TaskPromise scheduleAtFixedRate(Invocation invocation, Date startDate, long delay, TimeUnit timeUnit) {
		ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(() -> {
			processPool.execute(invocation);
		}, startDate, DateUtils.convertToMillis(delay, timeUnit));
		cache.putIfAbsent(invocation, new ScheduledTaskPromise(future));
		return cache.get(invocation);
	}

	@Override
	public boolean hasScheduled(Invocation invocation) {
		return cache.containsKey(invocation);
	}

	@Override
	public void cancel(Invocation invocation) {
		if (hasScheduled(invocation)) {
			cache.remove(invocation).cancel();
		}
	}

	@Override
	public int getCountOfScheduling() {
		return cache.size();
	}

}
