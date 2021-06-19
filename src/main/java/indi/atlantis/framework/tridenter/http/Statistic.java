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
package indi.atlantis.framework.tridenter.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

import com.github.paganini2008.devtools.multithreads.AtomicLongSequence;

import lombok.Data;

/**
 * 
 * Statistic
 *
 * @author Fred Feng
 * @version 1.0
 */
public final class Statistic {

	private final String provider;
	private final String path;
	final AtomicLongSequence total = new AtomicLongSequence();
	final AtomicLongSequence timeout = new AtomicLongSequence();
	final AtomicLongSequence failure = new AtomicLongSequence();
	final AtomicInteger qps = new AtomicInteger();
	private final Permit permit;

	public Statistic(String provider, String path, int maxPermits) {
		this.provider = provider;
		this.path = path;
		this.permit = new Permit(maxPermits);
		this.requestTimeRef = new AtomicStampedReference<RequestTime>(null, 0);
	}

	private AtomicStampedReference<RequestTime> requestTimeRef;
	private volatile int qpsValue;

	public String getProvider() {
		return provider;
	}

	public String getPath() {
		return path;
	}

	public long getTotalCount() {
		return total.get();
	}

	public long getTimeoutCount() {
		return timeout.get();
	}

	public long getFailedCount() {
		return failure.get();
	}

	void calculateQps() {
		final int current = qps.get();
		this.qpsValue = current;
		this.qps.getAndAdd(-1 * current);
	}

	public int getQps() {
		return qpsValue;
	}

	public Permit getPermit() {
		return permit;
	}

	public void setElapsed(long elapsed) {
		RequestTime current;
		RequestTime update = new RequestTime();
		do {
			current = requestTimeRef.getReference();
			if (current == null) {
				update = new RequestTime(elapsed);
			} else {
				update.setCount(current.getCount() + 1);
				update.setTotal(current.getTotal() + elapsed);
				update.setMaximum(Math.max(current.getMaximum(), elapsed));
				update.setMinimum(Math.min(current.getMinimum(), elapsed));
			}
		} while (!requestTimeRef.compareAndSet(current, update, requestTimeRef.getStamp(), requestTimeRef.getStamp() + 1));
	}

	public Map<String, Object> toEntries() {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("provider", provider);
		data.put("path", path);
		data.put("totalCount", getTotalCount());
		data.put("timeoutCount", getTimeoutCount());
		data.put("failedCount", getFailedCount());
		data.put("requestTime", requestTimeRef.getReference());
		data.put("qps", getQps());
		data.put("activePermits", permit.getMaxPermits() - permit.getAvailablePermits());
		data.put("maxPermits", permit.getMaxPermits());
		return data;
	}

	public static class Permit {

		private final AtomicInteger counter;
		private final int maxPermits;

		Permit(int maxPermits) {
			this.counter = new AtomicInteger(0);
			this.maxPermits = maxPermits;
		}

		public int accquire() {
			return counter.incrementAndGet();
		}

		public int accquire(int permits) {
			return counter.addAndGet(permits);
		}

		public int release() {
			return counter.decrementAndGet();
		}

		public int release(int permits) {
			return counter.addAndGet(-permits);
		}

		public int getAvailablePermits() {
			return maxPermits - counter.get();
		}

		public int getMaxPermits() {
			return maxPermits;
		}

	}

	@Data
	public static class RequestTime {

		RequestTime(long elapsed) {
			this.count = 1;
			this.total = elapsed;
			this.maximum = elapsed;
			this.minimum = elapsed;
		}

		RequestTime() {
		}

		private long maximum;
		private long minimum;
		private long total;
		private long count;

		public long getAverage() {
			return count > 0 ? total / count : 0;
		}
	}

}
