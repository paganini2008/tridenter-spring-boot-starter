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
import java.util.concurrent.TimeUnit;

import com.github.paganini2008.devtools.time.DateUtils;

/**
 * 
 * ScheduledProcessPool
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
public interface ScheduledProcessPool {

	TaskPromise schedule(Invocation invocation, Date startDate);

	default TaskPromise schedule(Invocation invocation, long delay, TimeUnit timeUnit) {
		return schedule(invocation, new Date(System.currentTimeMillis() + DateUtils.convertToMillis(delay, timeUnit)));
	}

	TaskPromise schedule(Invocation invocation, String cronExpression);

	TaskPromise scheduleWithFixedDelay(Invocation invocation, Date startDate, long delay, TimeUnit timeUnit);

	TaskPromise scheduleAtFixedRate(Invocation invocation, Date startDate, long delay, TimeUnit timeUnit);

	boolean hasScheduled(Invocation invocation);

	void cancel(Invocation invocation);
	
	int getCountOfScheduling();

}
