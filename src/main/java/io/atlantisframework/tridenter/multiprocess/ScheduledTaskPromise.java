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
package io.atlantisframework.tridenter.multiprocess;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ScheduledTaskPromise
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@Slf4j
public class ScheduledTaskPromise implements TaskPromise {

	private final ScheduledFuture<?> future;

	public ScheduledTaskPromise(ScheduledFuture<?> future) {
		this.future = future;
	}

	@Override
	public Object get(Supplier<Object> defaultValue) {
		try {
			return future.get();
		} catch (Exception e) {
			log.warn(e.getMessage());
			return defaultValue.get();
		}
	}

	@Override
	public Object get(long timeout, TimeUnit timeUnit, Supplier<Object> defaultValue) {
		try {
			return future.get(timeout, timeUnit);
		} catch (Exception e) {
			log.warn(e.getMessage());
			return defaultValue.get();
		}
	}

	@Override
	public void cancel() {
		try {
			future.cancel(false);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

}
