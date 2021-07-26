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
package indi.atlantis.framework.tridenter.multiprocess;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.github.paganini2008.devtools.ArrayUtils;
import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * MultiProcessingTaskPromise
 * 
 * @author Fred Feng
 *
 * @since 2.0.1
 */
@Slf4j
public class MultiProcessingTaskPromise implements TaskPromise, RedisMessageHandler {

	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final AtomicBoolean done = new AtomicBoolean(false);
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final String id;

	public MultiProcessingTaskPromise(String id) {
		this.id = id;
	}

	private volatile Object returnValue;

	@Override
	public Object get(Supplier<Object> defaultValue) {
		if (isDone()) {
			return getReturnValue(defaultValue);
		}
		while (!isCancelled()) {
			lock.lock();
			try {
				if (isDone()) {
					break;
				} else {
					try {
						condition.await(1, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						break;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return getReturnValue(defaultValue);
	}

	@Override
	public Object get(long timeout, TimeUnit timeUnit, Supplier<Object> defaultValue) {
		if (isDone()) {
			return getReturnValue(defaultValue);
		}
		final long begin = System.nanoTime();
		long elapsed;
		long nanosTimeout = TimeUnit.NANOSECONDS.convert(timeout, timeUnit);
		while (true) {
			lock.lock();
			try {
				if (isDone()) {
					break;
				} else {
					if (nanosTimeout > 0) {
						try {
							condition.awaitNanos(nanosTimeout);
						} catch (InterruptedException e) {
							break;
						}
						elapsed = (System.nanoTime() - begin);
						nanosTimeout -= elapsed;
					} else {
						break;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return getReturnValue(defaultValue);
	}

	private Object getReturnValue(Supplier<Object> defaultValue) {
		if (returnValue == null && defaultValue != null) {
			return defaultValue.get();
		}
		return returnValue;
	}

	@Override
	public void cancel() {
		lock.lock();
		try {
			cancelled.set(true);
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isCancelled() {
		return cancelled.get();
	}

	@Override
	public boolean isDone() {
		return done.get();
	}

	@Override
	public String getChannel() {
		return id;
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		if (message instanceof Return) {
			Return ret = (Return) message;
			returnValue = ret.getReturnValue();
			printf(ret.getInvocation());
		}
		lock.lock();
		try {
			done.set(true);
			condition.signalAll();
		} finally {
			lock.unlock();
		}

	}

	@Override
	public boolean isRepeatable() {
		return false;
	}

	private void printf(Invocation invocation) {
		if (log.isTraceEnabled()) {
			log.trace("[Calling: {}] Executed beanName: {}, beanClassName: {}, methodName: {}, Elapsed: {}", invocation.getId(),
					invocation.getSignature().getBeanName(), invocation.getSignature().getBeanClassName(),
					invocation.getSignature().getMethodName(), System.currentTimeMillis() - invocation.getTimestamp());
			log.trace("[Calling: {}] Input parameters: {}", invocation.getId(), ArrayUtils.toString(invocation.getArguments()));
		}
	}

}
